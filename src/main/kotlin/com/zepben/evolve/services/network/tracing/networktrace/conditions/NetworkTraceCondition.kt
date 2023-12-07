/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace.conditions

import com.zepben.evolve.services.network.tracing.networktrace.NetworkTraceStep
import com.zepben.evolve.services.network.tracing.networktrace.StepContext

internal interface NetworkTraceCondition<T> {
    fun stopCondition(item: NetworkTraceStep<T>, context: StepContext): Boolean = false
    fun queueCondition(item: NetworkTraceStep<T>, context: StepContext): Boolean = true

    val usesContextData: Boolean
    val contextDataKey: String get() = error { "INTERNAL ERROR: contextDataKey must be overridden when usesContextData return true" }
    fun computeNextContextData(nextItem: NetworkTraceStep<T>, key: String, context: StepContext): Any? = null
}