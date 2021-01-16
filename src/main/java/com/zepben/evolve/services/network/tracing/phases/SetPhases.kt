/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network.tracing.phases

import com.zepben.annotations.EverythingIsNonnullByDefault
import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.Feeder
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.cim.iec61970.base.wires.Breaker
import com.zepben.evolve.cim.iec61970.base.wires.EnergySource
import com.zepben.evolve.cim.iec61970.base.wires.SinglePhaseKind
import com.zepben.evolve.services.common.extensions.nameAndMRID
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.NetworkService.Companion.connectedTerminals
import com.zepben.evolve.services.network.tracing.OpenTest
import com.zepben.evolve.services.network.tracing.traversals.BasicTracker
import com.zepben.evolve.services.network.tracing.traversals.BranchRecursiveTraversal
import com.zepben.evolve.services.network.tracing.traversals.WeightedPriorityQueue
import org.slf4j.LoggerFactory
import java.util.stream.Collectors

/**
 * Convenience class that provides methods for setting phases on a [NetworkService]
 * This class is backed by a [BranchRecursiveTraversal].
 */
@Suppress("MemberVisibilityCanBePrivate")
@EverythingIsNonnullByDefault
class SetPhases {

    val normalTraversal = createTraversal(BranchRecursiveTraversal.QueueNext(::setNormalPhasesAndQueueNext))
    val currentTraversal = createTraversal(BranchRecursiveTraversal.QueueNext(::setCurrentPhasesAndQueueNext))

    private val feederCbErrorsLogged = mutableSetOf<String>()

    fun run(network: NetworkService) {
        //applyPhasesFromSources(network)
        setPhasesforEachHeadTerminal(network)
        val terminals = network.sequenceOf<EnergySource>()
            .filter { it.numPhases() > 0 }
            .flatMap { it.terminals.asSequence() }
            .toList()

        val breakers = network.listOf<Breaker>()
        runComplete(terminals, breakers)
    }

    fun run(conductingEquipment: ConductingEquipment, breakers: Collection<Breaker>) {
        if (conductingEquipment.numTerminals() == 0)
            return

        conductingEquipment.terminals.forEach { inTerminal ->
            val normalPhasesToFlow = getPhasesToFlow(inTerminal, OpenTest.NORMALLY_OPEN, PhaseSelector.NORMAL_PHASES)
            val currentPhasesToFlow = getPhasesToFlow(inTerminal, OpenTest.CURRENTLY_OPEN, PhaseSelector.CURRENT_PHASES)

            conductingEquipment.terminals.forEach { outTerminal ->
                if (outTerminal !== inTerminal) {
                    flowThroughEquipment(normalTraversal, inTerminal, outTerminal, normalPhasesToFlow, PhaseSelector.NORMAL_PHASES)
                    flowThroughEquipment(currentTraversal, inTerminal, outTerminal, currentPhasesToFlow, PhaseSelector.CURRENT_PHASES)
                }
            }
        }

        normalTraversal.tracker().clear()
        currentTraversal.tracker().clear()

        runComplete(conductingEquipment.terminals, breakers)
    }

    fun run(start: Terminal, breakers: Collection<Breaker>) {
        runComplete(listOf(start), breakers)
    }

    private fun createTraversal(queueNext: BranchRecursiveTraversal.QueueNext<Terminal>): BranchRecursiveTraversal<Terminal> =
        BranchRecursiveTraversal(queueNext,
            { WeightedPriorityQueue.processQueue { it.phases.numPhases() } },
            { BasicTracker() },
            { WeightedPriorityQueue.branchQueue { it.phases.numPhases() } }
        )

    private fun runComplete(startTerminals: List<Terminal>, breakers: Collection<Breaker>) {
        val feederCbs = breakers
            .stream()
            .filter(Breaker::isSubstationBreaker)
            .collect(Collectors.toSet())

        runComplete(startTerminals, feederCbs, normalTraversal, OpenTest.NORMALLY_OPEN, PhaseSelector.NORMAL_PHASES)
        runComplete(startTerminals, feederCbs, currentTraversal, OpenTest.CURRENTLY_OPEN, PhaseSelector.CURRENT_PHASES)
    }

    private fun runComplete(
        startTerminals: List<Terminal>,
        feederCbs: Set<Breaker>,
        traversal: BranchRecursiveTraversal<Terminal>,
        openTest: OpenTest,
        phaseSelector: PhaseSelector
    ) {
        for (terminal in startTerminals)
            runTerminal(terminal, traversal, phaseSelector)

        // We take a copy of the feeder CB's as we will modify the list while processing them.
        val processFeederCbs = feederCbs.toMutableSet()

        var keepProcessing = true
        while (keepProcessing) {
            val delayedFeederTraces = mutableListOf<DelayedFeederTrace>()

            processFeederCbs.removeIf { runFeederBreaker(it, traversal, openTest, phaseSelector, delayedFeederTraces) == FeederProcessingStatus.COMPLETE }
            delayedFeederTraces.forEach { runFromOutTerminal(traversal, it.outTerminal, it.phasesToFlow, phaseSelector) }

            keepProcessing = delayedFeederTraces.isNotEmpty()
        }
    }

    private fun runTerminal(start: Terminal, traversal: BranchRecursiveTraversal<Terminal>, phaseSelector: PhaseSelector) {
        val phasesToFlow = start.phases.singlePhases()
            .stream()
            .filter { phase -> phaseSelector.status(start, phase).direction().has(PhaseDirection.OUT) }
            .collect(Collectors.toSet())

        runFromOutTerminal(traversal, start, phasesToFlow, phaseSelector)
    }

    private fun runFromOutTerminal(
        traversal: BranchRecursiveTraversal<Terminal>,
        outTerminal: Terminal,
        phasesToFlow: Set<SinglePhaseKind>,
        phaseSelector: PhaseSelector
    ) {
        traversal.reset()
        traversal.tracker().visit(outTerminal)

        flowOutToConnectedTerminalsAndQueue(traversal, outTerminal, phasesToFlow, phaseSelector)

        traversal.run()
    }

    private fun runFeederBreaker(
        feederCb: Breaker,
        traversal: BranchRecursiveTraversal<Terminal>,
        openTest: OpenTest,
        phaseSelector: PhaseSelector,
        delayedFeederTraces: MutableList<DelayedFeederTrace>
    ): FeederProcessingStatus {
        if (feederCb.numTerminals() != 2 && feederCb.numTerminals() != 1) {
            if (feederCbErrorsLogged.add(feederCb.mRID)) {
                logger.warn(
                    "Ignoring feeder CB ${feederCb.nameAndMRID()} with ${feederCb.numTerminals()} terminals, expected 1 or 2 terminals."
                )
            }
            return FeederProcessingStatus.COMPLETE
        }

        if (feederCb.numTerminals() == 1) {
            setPhasesAndQueueNext(feederCb.terminals.first(), traversal, openTest, phaseSelector)
            return FeederProcessingStatus.COMPLETE
        }

        val processedPhases = mutableSetOf<SinglePhaseKind>()
        val statuses = getFeederCbTerminalPhasesByStatus(feederCb, openTest, phaseSelector)

        flowThroughFeederCbAndQueue(statuses[0], statuses[1], traversal, phaseSelector, delayedFeederTraces, processedPhases)
        flowThroughFeederCbAndQueue(statuses[1], statuses[0], traversal, phaseSelector, delayedFeederTraces, processedPhases)

        val nominalPhases = mutableSetOf<SinglePhaseKind>()
        feederCb.terminals.forEach { terminal -> nominalPhases.addAll(terminal.phases.singlePhases()) }

        return when {
            processedPhases.size == nominalPhases.size -> FeederProcessingStatus.COMPLETE
            processedPhases.isNotEmpty() -> FeederProcessingStatus.PARTIAL
            else -> FeederProcessingStatus.NONE
        }
    }

    private fun setPhasesforEachHeadTerminal(network: NetworkService) {
        network.sequenceOf<Feeder>().forEach(::setPhasesforHeadTerminal)
    }

    private fun setPhasesforHeadTerminal(feeder: Feeder) {
        val headTerminal = feeder.normalHeadTerminal
        if (headTerminal == null) {
            logger.warn(
                "Setting of phases for headTerminal ${headTerminal} fails." +
                    "phases of headTerminal is None!" +
                    "Tracing of phases for Feeder ${feeder.mRID} is not feasible!"
            )
            return
        }
        val nominalPhases = mutableSetOf<SinglePhaseKind>()
        nominalPhases.addAll(headTerminal.phases.singlePhases())

        headTerminal.phases.singlePhases().forEach{phase ->
            headTerminal.normalPhases(phase).add(phase, PhaseDirection.OUT)
            headTerminal.currentPhases(phase).add(phase, PhaseDirection.OUT)
        }
    }


    private fun applyPhasesFromSources(network: NetworkService) {
        network.sequenceOf<EnergySource>().filter { it.numPhases() > 0 }.forEach(::applyPhasesFromSource)
    }

    private fun applyPhasesFromSource(energySource: EnergySource) {
        if (energySource.numTerminals() == 0)
            return

        val energySourcePhases = mutableSetOf<SinglePhaseKind>()
        energySource.phases.forEach { energySourcePhases.add(it.phase) }

        val nominalPhases = mutableSetOf<SinglePhaseKind>()
        energySource.terminals.forEach { terminal -> nominalPhases.addAll(terminal.phases.singlePhases()) }

        if (energySourcePhases.size != nominalPhases.size) {
            logger.warn(
                "Energy source ${energySource.nameAndMRID()} is a source with ${energySourcePhases.size} phases and ${nominalPhases.size} nominal phases. " +
                    "Number of phases should match number of nominal phases!"
            )
        }

        energySource.terminals.forEach { terminal ->
            terminal.phases.singlePhases().forEach { phase ->
                terminal.normalPhases(phase).add(phase, PhaseDirection.OUT)
                terminal.currentPhases(phase).add(phase, PhaseDirection.OUT)
            }
        }
    }

    private fun setNormalPhasesAndQueueNext(current: Terminal?, traversal: BranchRecursiveTraversal<Terminal>) =
        setPhasesAndQueueNext(current!!, traversal, OpenTest.NORMALLY_OPEN, PhaseSelector.NORMAL_PHASES)

    private fun setCurrentPhasesAndQueueNext(current: Terminal?, traversal: BranchRecursiveTraversal<Terminal>) =
        setPhasesAndQueueNext(current!!, traversal, OpenTest.CURRENTLY_OPEN, PhaseSelector.CURRENT_PHASES)

    private fun setPhasesAndQueueNext(current: Terminal, traversal: BranchRecursiveTraversal<Terminal>, openTest: OpenTest, phaseSelector: PhaseSelector) {
        val phasesToFlow = getPhasesToFlow(current, openTest, phaseSelector)

        current.conductingEquipment?.terminals?.forEach { outTerminal ->
            if (outTerminal != current && flowThroughEquipment(traversal, current, outTerminal, phasesToFlow, phaseSelector))
                flowOutToConnectedTerminalsAndQueue(traversal, outTerminal, phasesToFlow, phaseSelector)
        }
    }

    private fun flowThroughFeederCbAndQueue(
        inTerminal: FeederCbTerminalPhasesByStatus,
        outTerminal: FeederCbTerminalPhasesByStatus,
        traversal: BranchRecursiveTraversal<Terminal>,
        phaseSelector: PhaseSelector,
        delayedFeederTraces: MutableList<DelayedFeederTrace>,
        processedPhases: MutableSet<SinglePhaseKind>
    ) {
        if (inTerminal.inPhases.isEmpty())
            return

        val phasesToFlow = inTerminal.phasesToFlow.toMutableSet()
        for (phase in inTerminal.terminal.phases.singlePhases()) {
            if (inTerminal.inPhases.contains(phase)) {
                processedPhases.add(phase)

                // Remove any phases that have already been processed from the other side.
                if (!outTerminal.nonePhases.contains(phase))
                    phasesToFlow.remove(phase)
            }
        }

        if (flowThroughEquipment(traversal, inTerminal.terminal, outTerminal.terminal, phasesToFlow, phaseSelector))
            delayedFeederTraces.add(DelayedFeederTrace(outTerminal.terminal, phasesToFlow))
    }

    /**
     * Applies all the in phases from the inTerminal to the out terminals.
     */
    private fun flowThroughEquipment(
        traversal: BranchRecursiveTraversal<Terminal>,
        inTerminal: Terminal,
        outTerminal: Terminal,
        phasesToFlow: Set<SinglePhaseKind>,
        phaseSelector: PhaseSelector
    ): Boolean {
        traversal.tracker().visit(outTerminal)

        var hasChanges = false
        phasesToFlow.forEach {
            hasChanges = try {
                phaseSelector.status(outTerminal, it).add(phaseSelector.status(inTerminal, it).phase(), PhaseDirection.OUT) || hasChanges
            } catch (ex: UnsupportedOperationException) {
                throw IllegalStateException(
                    "Attempted to apply more than one phase to ${outTerminal.conductingEquipment?.mRID ?: inTerminal.mRID} on nominal phase $it. " +
                        "Detected phases ${phaseSelector.status(outTerminal, it).phase()} and ${phaseSelector.status(inTerminal, it).phase()}."
                )
            }
        }

        return hasChanges
    }

    /**
     * Applies all the out phases from the outTerminal to the connected in terminals and queues them up.
     */
    private fun flowOutToConnectedTerminalsAndQueue(
        traversal: BranchRecursiveTraversal<Terminal>,
        outTerminal: Terminal,
        phasesToFlow: Set<SinglePhaseKind>,
        phaseSelector: PhaseSelector
    ) {
        // Get all the connected terminals to the nominal phases with phases going out
        val connectivityResults = connectedTerminals(outTerminal, phasesToFlow)
        for (connectivityResult in connectivityResults) {
            val inTerminal = connectivityResult.toTerminal
            var hasAdded = false

            for (oi in connectivityResult.nominalPhasePaths) {
                try {
                    if (phaseSelector.status(inTerminal, oi.to).add(phaseSelector.status(outTerminal, oi.from).phase(), PhaseDirection.IN))
                        hasAdded = true
                } catch (ex: UnsupportedOperationException) {
                    throw IllegalStateException(
                        "Attempted to apply more than one phase to ${inTerminal.conductingEquipment?.mRID ?: inTerminal.mRID} on nominal phase ${oi.to}. " +
                            "Attempted to apply phase ${phaseSelector.status(outTerminal, oi.from).phase()} " +
                            "to ${phaseSelector.status(inTerminal, oi.to).phase()}."
                    )
                }
            }

            // The hasAdded check is to stop tracing network that has already had its phasing applied on a previous branch.
            // The visited check is to stop the trace applying phases back along the source path if there is a loop.
            if (hasAdded && !traversal.hasVisited(inTerminal)) {
                if (connectivityResults.size > 1 || outTerminal.conductingEquipment?.numTerminals() ?: 0 > 2)
                    traversal.branchQueue().add(traversal.branchSupplier().get().setStart(inTerminal))
                else
                    traversal.queue().add(inTerminal)
            }
        }
    }

    private fun getPhasesToFlow(terminal: Terminal, openTest: OpenTest, phaseSelector: PhaseSelector): MutableSet<SinglePhaseKind> {
        val phasesToFlow = mutableSetOf<SinglePhaseKind>()
        if (terminal.conductingEquipment is Breaker && (terminal.conductingEquipment as Breaker).isSubstationBreaker)
            return phasesToFlow

        val conductingEquipment: ConductingEquipment = terminal.conductingEquipment ?: return phasesToFlow

        for (phase in terminal.phases.singlePhases()) {
            if (!openTest.isOpen(conductingEquipment, phase) && phaseSelector.status(terminal, phase).direction().has(PhaseDirection.IN))
                phasesToFlow.add(phase)
        }

        return phasesToFlow
    }

    private fun getFeederCbTerminalPhasesByStatus(feederCb: Breaker, openTest: OpenTest, phaseSelector: PhaseSelector): List<FeederCbTerminalPhasesByStatus> {
        val results = mutableListOf<FeederCbTerminalPhasesByStatus>()

        feederCb.terminals.forEach { terminal ->
            val status = FeederCbTerminalPhasesByStatus(terminal)
            results.add(status)

            terminal.phases.singlePhases().forEach {
                val phaseStatus = phaseSelector.status(terminal, it)
                if (phaseStatus.direction() === PhaseDirection.IN) {
                    status.addInPhase(it)
                    if (!openTest.isOpen(feederCb, it))
                        status.addPhaseToFlow(it)
                } else if (phaseStatus.direction() === PhaseDirection.BOTH)
                    status.addInOutPhase(it)
                else if (phaseStatus.direction() === PhaseDirection.NONE)
                    status.addNonePhase(it)
            }
        }

        return results
    }

    private enum class FeederProcessingStatus {
        COMPLETE, PARTIAL, NONE
    }

    private class FeederCbTerminalPhasesByStatus(val terminal: Terminal) {
        val inPhases = mutableSetOf<SinglePhaseKind>()
        val nonePhases = mutableSetOf<SinglePhaseKind>()
        val phasesToFlow = mutableSetOf<SinglePhaseKind>()

        fun addInPhase(phase: SinglePhaseKind) {
            inPhases.add(phase)
        }

        fun addInOutPhase(phase: SinglePhaseKind) {
            inPhases.add(phase)
        }

        fun addNonePhase(phase: SinglePhaseKind) {
            nonePhases.add(phase)
        }

        fun addPhaseToFlow(phase: SinglePhaseKind) {
            phasesToFlow.add(phase)
        }
    }

    private class DelayedFeederTrace(
        val outTerminal: Terminal,
        val phasesToFlow: Set<SinglePhaseKind>
    )

    companion object {
        private val logger = LoggerFactory.getLogger(SetPhases::class.java)
    }
}
