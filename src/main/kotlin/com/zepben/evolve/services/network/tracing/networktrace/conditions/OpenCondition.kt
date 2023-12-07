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
import com.zepben.evolve.services.network.tracing.networktrace.StepContext

internal class OpenCondition<T>(
    private val openTest: OpenTest,
    private val phase: SinglePhaseKind? = null
) : NetworkTraceCondition<T> {
    override fun stopCondition(item: NetworkTraceStep<T>, context: StepContext): Boolean =
        if (item.steppedInternally) {
            openTest.isOpen(item.fromEquipment, phase)
        } else {
            openTest.isOpen(item.toEquipment, phase)
        }

    override fun queueCondition(item: NetworkTraceStep<T>, context: StepContext): Boolean =
        if (item.steppedInternally) {
            !openTest.isOpen(item.toEquipment, phase)
        } else {
            true
        }

    override val usesContextData: Boolean get() = false
}
