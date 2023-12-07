/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace.conditions

import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.services.network.tracing.feeder.FeederDirection
import com.zepben.evolve.services.network.tracing.networktrace.NetworkTraceStep
import com.zepben.evolve.services.network.tracing.networktrace.StepContext

internal class DirectionCondition<T>(
    val direction: FeederDirection,
    val getDirection: Terminal.() -> FeederDirection
) : NetworkTraceCondition<T> {
    override fun stopCondition(item: NetworkTraceStep<T>, context: StepContext): Boolean =
        if (item.steppedInternally) {
            direction !in item.toTerminal.getDirection()
        } else {
            item.toTerminal.otherTerminals().none { direction in it.getDirection() }
        }

    override fun queueCondition(item: NetworkTraceStep<T>, context: StepContext): Boolean =
        if (item.steppedInternally) {
            direction in item.toTerminal.getDirection()
        } else {
            direction in item.fromTerminal.getDirection()
        }

    override val usesContextData: Boolean get() = false
}