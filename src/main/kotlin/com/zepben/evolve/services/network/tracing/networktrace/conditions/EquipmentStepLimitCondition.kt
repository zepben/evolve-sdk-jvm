/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace.conditions

import com.zepben.evolve.services.network.tracing.networktrace.NetworkTraceStep
import com.zepben.evolve.services.network.tracing.traversalV2.StepContext
import com.zepben.evolve.services.network.tracing.traversalV2.StopCondition

class EquipmentStepLimitCondition<T>(
    private val limit: Int
) : StopCondition<NetworkTraceStep<T>> {
    override fun shouldStop(item: NetworkTraceStep<T>, context: StepContext): Boolean {
        return item.path.numEquipmentSteps >= limit
    }
}
