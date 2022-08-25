/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing

import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.Feeder
import com.zepben.evolve.cim.iec61970.base.wires.PowerTransformer
import com.zepben.evolve.cim.iec61970.base.wires.Switch
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.tracing.connectivity.ConductingEquipmentStep
import com.zepben.evolve.services.network.tracing.connectivity.ConnectedEquipmentTraversal

/**
 * A class which can be used for finding the SWER equipment in a [NetworkService] or [Feeder].
 */
class FindSwerEquipment(
    private val createTrace: () -> ConnectedEquipmentTraversal = { Tracing.normalConnectedEquipmentTrace() }
) {

    /**
     * Find the [ConductingEquipment] on any [Feeder] in a [NetworkService] which is SWER. This will include any equipment on the LV network that is energised
     * via SWER.
     *
     * @param networkService The [NetworkService] to process.
     *
     * @return A [List] of [ConductingEquipment] on any [Feeder] in [networkService] that is SWER, or energised via SWER.
     */
    fun find(networkService: NetworkService): List<ConductingEquipment> =
        networkService.sequenceOf<Feeder>()
            .flatMap { find(it) }
            .distinct()
            .toList()

    /**
     * Find the [ConductingEquipment] on a [Feeder] which is SWER. This will include any equipment on the LV network that is energised via SWER.
     *
     * @param feeder The [Feeder] to process.
     *
     * @return A [List] of [ConductingEquipment] on [feeder] that is SWER, or energised via SWER.
     */
    fun find(feeder: Feeder): List<ConductingEquipment> {
        val swerEquipment = mutableSetOf<ConductingEquipment>()

        feeder.equipment
            .asSequence()
            .filterIsInstance<PowerTransformer>()
            .filter { it.hasSwerTerminal }
            .filter { it.hasNonSwerTerminal }
            .forEach { traceFrom(it, swerEquipment) }

        return swerEquipment.toList()
    }

    private fun traceFrom(transformer: PowerTransformer, swerEquipment: MutableSet<ConductingEquipment>) {
        // We will start from the equipment attached to this transformer rather than the transformer itself, so add it to the SWER equipment.
        swerEquipment.add(transformer)

        // Trace from any SWER terminals.
        traceSwerFrom(transformer, swerEquipment)

        // Trace from any LV terminals.
        traceLvFrom(transformer, swerEquipment)
    }

    private fun traceSwerFrom(transformer: PowerTransformer, swerEquipment: MutableSet<ConductingEquipment>) {
        val trace = createTrace().apply {
            addStopCondition { it.conductingEquipment in swerEquipment }
            addStopCondition { !it.hasSwerTerminal }
            addStepAction { it, isStopping ->
                // To make sure we include any open points on a SWER network (unlikely) we include stop equipment if it is a [Switch].
                if (!isStopping || (it.conductingEquipment is Switch))
                    swerEquipment.add(it.conductingEquipment)
            }
        }

        transformer.terminals
            .asSequence()
            .filter { it.phases.numPhases() == 1 }
            .flatMap { it.connectedTerminals() }
            .mapNotNull { it.conductingEquipment }
            .forEach { trace.run(it) }
    }

    private fun traceLvFrom(transformer: PowerTransformer, swerEquipment: MutableSet<ConductingEquipment>) {
        val trace = createTrace().apply {
            addStopCondition { it.conductingEquipment in swerEquipment }
            addStepAction { swerEquipment.add(it.conductingEquipment) }
        }

        transformer.terminals
            .asSequence()
            .filter { it.phases.numPhases() > 1 }
            .flatMap { it.connectedTerminals() }
            .mapNotNull { it.conductingEquipment }
            .filter { it.baseVoltageValue in 1..1000 }
            .forEach { trace.run(it) }
    }

    private val ConductingEquipmentStep.hasSwerTerminal: Boolean get() = conductingEquipment.hasSwerTerminal

    private val ConductingEquipment.hasSwerTerminal: Boolean get() = terminals.any { it.phases.numPhases() == 1 }
    private val ConductingEquipment.hasNonSwerTerminal: Boolean get() = terminals.any { it.phases.numPhases() > 1 }

}
