/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace

import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.cim.iec61970.base.wires.SinglePhaseKind
import com.zepben.evolve.services.network.tracing.OpenTest
import com.zepben.evolve.services.network.tracing.feeder.FeederDirection
import com.zepben.evolve.services.network.tracing.networktrace.conditions.DirectionCondition
import com.zepben.evolve.services.network.tracing.networktrace.conditions.EquipmentStepLimitCondition
import com.zepben.evolve.services.network.tracing.networktrace.conditions.NetworkTraceCondition
import com.zepben.evolve.services.network.tracing.networktrace.conditions.OpenCondition
import com.zepben.evolve.services.network.tracing.traversals.*

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
        setStart(NetworkTraceStep(start, start, 0, 0, context))
        run(canStopOnStartItem)
    }

    fun run(start: ConductingEquipment, canStopOnStartItem: Boolean = true) {
        // TODO: How to support multiple start items?
        start.terminals.forEach { queueItem(NetworkTraceStep(it, it, 0, 0, null), StepContext()) }
        run(canStopOnStartItem)
    }

    fun normallyUpstream(): NetworkTrace<T> {
        addCondition(DirectionCondition(FeederDirection.UPSTREAM, Terminal::normalFeederDirection))
        return this
    }

    fun currentlyUpstream(): NetworkTrace<T> {
        addCondition(DirectionCondition(FeederDirection.UPSTREAM, Terminal::currentFeederDirection))
        return this
    }

    fun normallyDownstream(): NetworkTrace<T> {
        addCondition(DirectionCondition(FeederDirection.DOWNSTREAM, Terminal::normalFeederDirection))
        return this
    }

    fun currentlyDownstream(): NetworkTrace<T> {
        addCondition(DirectionCondition(FeederDirection.DOWNSTREAM, Terminal::currentFeederDirection))
        return this
    }

    fun stopAtNormallyOpen(phase: SinglePhaseKind? = null): NetworkTrace<T> {
        addCondition(OpenCondition(OpenTest.NORMALLY_OPEN, phase))
        return this
    }

    fun stopAtCurrentlyOpen(phase: SinglePhaseKind? = null): NetworkTrace<T> {
        addCondition(OpenCondition(OpenTest.CURRENTLY_OPEN, phase))
        return this
    }

    fun limitSteps(limit: Int, equipmentType: Class<out ConductingEquipment>? = null): NetworkTrace<T> {
        addCondition(EquipmentStepLimitCondition(limit, equipmentType))
        return this
    }

    private fun addCondition(condition: NetworkTraceCondition<T>) {
        addStopCondition(condition::stopCondition)
        addQueueCondition(condition::queueCondition)

        if (condition.usesContextData) {
            addComputeNextContext(condition.contextDataKey, condition::computeNextContextData)
        }
    }

    override fun getDerivedThis(): NetworkTrace<T> = this
}
