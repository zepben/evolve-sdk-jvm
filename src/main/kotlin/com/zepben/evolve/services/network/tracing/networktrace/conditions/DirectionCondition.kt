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
import com.zepben.evolve.services.network.tracing.networktrace.TerminalToTerminalPath
import com.zepben.evolve.services.network.tracing.traversalV2.QueueCondition
import com.zepben.evolve.services.network.tracing.traversalV2.StepContext

internal class DirectionCondition<T>(
    val direction: FeederDirection,
    val getDirection: Terminal.() -> FeederDirection
) : QueueCondition<NetworkTraceStep<T>> {

    override fun shouldQueue(nextItem: NetworkTraceStep<T>, currentContext: StepContext): Boolean =
        when (val path = nextItem.path) {
            is TerminalToTerminalPath -> {
                if (path.tracedInternally) {
                    direction in path.toTerminal.getDirection()
                } else {
                    direction in path.fromTerminal.getDirection()
                }
            }
        }
}
