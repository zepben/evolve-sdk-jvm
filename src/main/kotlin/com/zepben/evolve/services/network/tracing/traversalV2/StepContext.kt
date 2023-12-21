/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.traversalV2

open class StepContext(
    val isStartItem: Boolean,
    val stepNumber: Int = 0,
    private var customData: MutableMap<String, Any?>? = null
) {
    var isStopping: Boolean = false
        internal set

    fun setData(key: String, value: Any?) {
        customData = customData ?: mutableMapOf()
        customData!![key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getData(key: String): T? {
        return customData?.get(key) as? T?
    }
}