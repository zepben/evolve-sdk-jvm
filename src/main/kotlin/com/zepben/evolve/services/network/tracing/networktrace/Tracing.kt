/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace

import com.zepben.evolve.services.network.tracing.traversals.BasicQueue
import com.zepben.evolve.services.network.tracing.traversals.TraversalQueue

object Tracing {

    fun <T> connectedEquipmentTrace(queue: TraversalQueue<NetworkTraceStep<T>> = BasicQueue.depthFirst()): NetworkTrace<T> {
        val queueNext = NetworkTrace.QueueNext<T> { ts, ctx, queueItem, t ->
            val path = ts.path
            path.toEquipment.terminals
                .filter { it != path.toTerminal }
                .flatMap { it.connectedTerminals() }
                .map {
                    val nextPath = when (path) {
                        is TerminalToTerminalPath -> {
                            TerminalToTerminalPath(
                                path.toTerminal,
                                it,
                                path.numTerminalSteps + 1,
                                path.numEquipmentSteps + 1,
                            )
                        }
                    }

                    NetworkTraceStep(nextPath, t.computeNextData(ts, ctx, nextPath))
                }
                .forEach { queueItem(it) }
        }

        return NetworkTrace(queueNext, queue, NetworkTraceTracker { it.path.toEquipment })
    }

    fun <T> connectedTerminalTrace(): NetworkTrace<T> {
        val queueNext = NetworkTrace.QueueNext<T> { ts, ctx, queueItem, t ->
            // Check if we last moved between equipment, or across it.
            // TODO: Should we handle the TerminalToTerminalTraceStep cast here?
            val path = ts.path
            val terminals = if (path.tracedInternally) path.toTerminal?.connectedTerminals() else path.toTerminal?.otherTerminals()
            terminals?.forEach {
                val nextPath = when (path) {
                    is TerminalToTerminalPath -> {
                        TerminalToTerminalPath(
                            path.toTerminal,
                            it,
                            path.numTerminalSteps + 1,
                            if (path.tracedInternally) path.numEquipmentSteps else path.numEquipmentSteps + 1,
                        )
                    }
                }

                val nextStep = NetworkTraceStep(nextPath, t.computeNextData(ts, ctx, nextPath))
                queueItem(nextStep)
            }
        }

        return NetworkTrace(queueNext, BasicQueue.depthFirst(), NetworkTraceTracker { it.path.toTerminal })
    }

}

