/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace.conditions

import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.services.network.tracing.networktrace.NetworkTraceStep
import com.zepben.evolve.services.network.tracing.networktrace.StepContext

class EquipmentStepLimitCondition<T>(
    private val limit: Int,
    private val equipmentType: Class<out ConductingEquipment>? = null
) : NetworkTraceCondition<T> {
    override fun stopCondition(item: NetworkTraceStep<T>, context: StepContext): Boolean {
        return if (equipmentType == null) {
            item.nEquipmentSteps >= limit
        } else {
            context.getEquipmentTypeSteps() >= limit
        }
    }

    override val usesContextData: Boolean get() = equipmentType != null

    override val contextDataKey: String
        get() = "sdk:nSteps${equipmentType?.simpleName ?: ""}"

    override fun computeNextContextData(nextItem: NetworkTraceStep<T>, key: String, context: StepContext): Any? {
        if (equipmentType == null)
            return null

        val nSteps = context.getEquipmentTypeSteps()
        return when {
            nextItem.steppedInternally -> nSteps
            nextItem.toEquipment::class.java.isAssignableFrom(equipmentType) -> nSteps + 1
            else -> nSteps
        }
    }

    private fun StepContext.getEquipmentTypeSteps(): Int {
        val key = requireNotNull(contextDataKey) { "INTERAL ERROR: Custom context key should always exist here" }
        return this.getData<Int>(key) ?: 0
    }
}