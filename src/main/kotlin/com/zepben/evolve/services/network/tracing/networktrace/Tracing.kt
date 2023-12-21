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
            ts.toEquipment.terminals
                .filter { it != ts.toTerminal }
                .flatMap { it.connectedTerminals() }
                .map {
                    when (ts) {
                        is TerminalToTerminalTraceStep -> {
                            TerminalToTerminalTraceStep(
                                ts.toTerminal,
                                it,
                                ts.nTerminalSteps + 1,
                                ts.nEquipmentSteps + 1,
                                t.computeNextData(ts, it, ctx),
                            )
                        }
                    }
                }
                .forEach { queueItem(it) }
        }

        return NetworkTrace(queueNext, queue, NetworkTraceTracker { it.toEquipment })
    }

    fun <T> connectedTerminalTrace(): NetworkTrace<T> {
        val queueNext = NetworkTrace.QueueNext<T> { ts, ctx, queueItem, t ->
            // Check if we last moved between equipment, or across it.
            // TODO: Should we handle the TerminalToTerminalTraceStep cast here?
            val terminals = if (ts.steppedInternally) ts.toTerminal?.connectedTerminals() else ts.toTerminal?.otherTerminals()
            terminals?.forEach {
                val nextStep = when (ts) {
                    is TerminalToTerminalTraceStep -> {
                        TerminalToTerminalTraceStep(
                            ts.toTerminal,
                            it,
                            ts.nTerminalSteps + 1,
                            if (ts.steppedInternally) ts.nEquipmentSteps else ts.nEquipmentSteps + 1,
                            t.computeNextData(ts, it, ctx)
                        )
                    }
                }

                queueItem(nextStep)
            }
        }

        return NetworkTrace(queueNext, BasicQueue.depthFirst(), NetworkTraceTracker { it.toTerminal })
    }

}

