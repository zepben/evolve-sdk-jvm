/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace.conditions

import com.zepben.evolve.cim.iec61970.base.wires.SinglePhaseKind
import com.zepben.evolve.services.network.tracing.OpenTest
import com.zepben.evolve.services.network.tracing.networktrace.NetworkTraceStep
import com.zepben.evolve.services.network.tracing.traversalV2.QueueCondition
import com.zepben.evolve.services.network.tracing.traversalV2.StepContext

internal class OpenCondition<T>(
    private val openTest: OpenTest,
    private val phase: SinglePhaseKind? = null
) : QueueCondition<NetworkTraceStep<T>> {
    override fun shouldQueue(nextItem: NetworkTraceStep<T>, currentContext: StepContext): Boolean =
        if (nextItem.path.tracedInternally) {
            !openTest.isOpen(nextItem.path.toEquipment, phase)
        } else {
            true
        }
}
