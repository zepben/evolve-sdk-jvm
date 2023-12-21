/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.traversalV2

// TODO: I don't really like this name. Someone please suggest something else...
interface ContextDataComputer<T> {
    val key: String

    fun computeInitialValue(nextItem: T): Any?

    fun computeNextValue(nextItem: T, stepValue: Any?)
}

interface TypedContextDataComputer<T, U> : ContextDataComputer<T> {
    @Suppress("UNCHECKED_CAST")
    override fun computeNextValue(nextItem: T, stepValue: Any?) {
        computeNextValue(nextItem, stepValue as U)
    }

    override fun computeInitialValue(nextItem: T): U

    fun computeNextValue(nextItem: T, value: U): U
}