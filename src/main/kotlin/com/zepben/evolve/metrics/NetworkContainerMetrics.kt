/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

data class NetworkContainerMetrics (
    var numDistTx: Int? = null
) {

    private val fieldsBySqlName: Map<String, KMutableProperty<out Number?>> = mapOf(
        "NUM_DIST_TX" to ::numDistTx
    )

    val entries: List<Pair<String, Number>> = fieldsBySqlName.map { (name, prop) ->
        prop.getter.call()?.let { name to it }
    }.filterNotNull()

    operator fun set(fieldName: String, value: Number?) {
        fieldsBySqlName[fieldName]?.let {
            if (it.returnType.isSubtypeOf(Int::class.createType(nullable = true))) {
                it.setter.call(value?.toInt())
            } else if (it.returnType.isSubtypeOf(Float::class.createType(nullable = true))) {
                it.setter.call(value?.toFloat())
            } else it.setter.call(value)
        } ?: throw IllegalArgumentException("No network container metric named '$fieldName'. Please upgrade the SDK if needed.")
    }

}
