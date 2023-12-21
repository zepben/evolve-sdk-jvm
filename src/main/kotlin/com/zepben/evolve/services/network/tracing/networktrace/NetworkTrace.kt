/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace

import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.PhaseCode
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.cim.iec61970.base.wires.SinglePhaseKind
import com.zepben.evolve.services.network.tracing.OpenTest
import com.zepben.evolve.services.network.tracing.feeder.FeederDirection
import com.zepben.evolve.services.network.tracing.networktrace.conditions.*
import com.zepben.evolve.services.network.tracing.networktrace.conditions.DirectionCondition
import com.zepben.evolve.services.network.tracing.networktrace.conditions.OpenCondition
import com.zepben.evolve.services.network.tracing.traversalV2.StepContext
import com.zepben.evolve.services.network.tracing.traversalV2.TraversalV2
import com.zepben.evolve.services.network.tracing.traversals.Tracker
import com.zepben.evolve.services.network.tracing.traversals.TraversalQueue

class NetworkTrace<T>(
    queueNext: QueueNext<T>,
    queue: TraversalQueue<NetworkTraceStep<T>>,
    tracker: Tracker<NetworkTraceStep<T>>
) : TraversalV2<NetworkTraceStep<T>, NetworkTrace<T>>(queueNext, queue, tracker) {

    fun interface QueueNext<T> : TraversalV2.QueueNext<NetworkTraceStep<T>, NetworkTrace<T>>

    private var computeData: ((NetworkTraceStep<T>, Terminal, context: StepContext) -> T)? = null

    fun setComputeData(computeContext: ((NetworkTraceStep<T>, Terminal, context: StepContext) -> T)?): NetworkTrace<T> {
        this.computeData = computeContext
        return this
    }

    fun clearComputeData(): NetworkTrace<T> {
        this.computeData = null
        return this
    }

    fun computeNextData(step: NetworkTraceStep<T>, nextTerminal: Terminal, context: StepContext): T? =
        computeData?.invoke(step, nextTerminal, context)

    fun run(start: Terminal, canStopOnStartItem: Boolean = true, context: T? = null) {
        setStart(TerminalToTerminalTraceStep(start, start, 0, 0, context))
        run(canStopOnStartItem)
    }

    fun run(start: ConductingEquipment, canStopOnStartItem: Boolean = true) {
        // TODO: How to support multiple start items?
//        start.terminals.forEach { queueItem(NetworkTraceStep(it, it, 0, 0, null), StepContext()) }
//        run(canStopOnStartItem)
    }

    fun normallyUpstream(): NetworkTrace<T> {
        addQueueCondition(DirectionCondition(FeederDirection.UPSTREAM, Terminal::normalFeederDirection))
        return this
    }

    fun currentlyUpstream(): NetworkTrace<T> {
        addQueueCondition(DirectionCondition(FeederDirection.UPSTREAM, Terminal::currentFeederDirection))
        return this
    }

    fun normallyDownstream(): NetworkTrace<T> {
        addQueueCondition(DirectionCondition(FeederDirection.DOWNSTREAM, Terminal::normalFeederDirection))
        return this
    }

    fun currentlyDownstream(): NetworkTrace<T> {
        addQueueCondition(DirectionCondition(FeederDirection.DOWNSTREAM, Terminal::currentFeederDirection))
        return this
    }

    fun stopAtNormallyOpen(phase: SinglePhaseKind? = null): NetworkTrace<T> {
        addQueueCondition(OpenCondition(OpenTest.NORMALLY_OPEN, phase))
        return this
    }

    fun stopAtCurrentlyOpen(phase: SinglePhaseKind? = null): NetworkTrace<T> {
        addQueueCondition(OpenCondition(OpenTest.CURRENTLY_OPEN, phase))
        return this
    }

    fun limitEquipmentSteps(limit: Int): NetworkTrace<T> {
        addStopCondition(EquipmentStepLimitCondition(limit))
        return this
    }

    fun limitEquipmentSteps(limit: Int, equipmentType: Class<out ConductingEquipment>): NetworkTrace<T> {
        addStopCondition(EquipmentTypeStepLimitCondition(limit, equipmentType))
        return this
    }

    fun withPhases(phases: PhaseCode): NetworkTrace<T> {
        addQueueCondition(PhaseCondition(phases))
        return this
    }

    override fun getDerivedThis(): NetworkTrace<T> = this
}
