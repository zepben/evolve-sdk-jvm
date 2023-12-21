/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.traversalV2

fun interface StepAction<T> {
    fun apply(item: T, context: StepContext)
}

abstract class StepActionWithContextData<T, U> : StepAction<T>, TypedContextDataComputer<T, U>