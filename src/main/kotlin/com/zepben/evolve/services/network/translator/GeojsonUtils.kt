/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.translator

import kotlinx.serialization.json.*


fun Map<String, JsonElement>.getIntList(key: String): List<Int>? = this[key]?.let { if (it is JsonArray) it.filterIsInstance(Int::class.java) else null }

fun Map<String, JsonElement>.getStringList(key: String): List<String>? = this[key]?.let {
    if (it is JsonArray)
        it.filterIsInstance(JsonPrimitive::class.java).map { json -> json.content }
    else
        null
}

fun Map<String, JsonElement>.getList(key: String): MutableList<JsonElement>? = this[key].let {
    if (it is JsonArray)
        it.toMutableList()
    else
        null
}

fun Map<String, JsonElement>.getMap(key: String): MutableMap<String, JsonElement>? = this[key].let {
    if (it is JsonObject)
        it.toMutableMap()
    else
        null
}

/**
 * Gets a list of Json objects from the map. This should only be used in conjunction with deserialising JsonElements.
 * We perform an unchecked cast to the map but as this is on a map of String -> JsonElement the resulting Map will
 * always have values that are valid JsonElements.
 *
 * Warning: This does not support lists of maps where the keys are not Strings and will throw a ClassCastException if you use keys other than Strings in your json.
 */
fun Map<String, JsonElement>.getMapList(key: String): MutableList<MutableMap<String, JsonElement>>? = this[key].let {
    if (it is JsonArray)
        it.filterIsInstance(JsonObject::class.java).map { map ->
            map.toMutableMap()
        } as MutableList
    else
        null
}

fun MutableMap<String, JsonElement>.put(key: String, value: String) {
    this[key] = JsonPrimitive(value)
}

fun MutableMap<String, JsonElement>.put(key: String, value: Number) {
    this[key] = JsonPrimitive(value)
}

fun MutableMap<String, JsonElement>.put(key: String, value: Boolean) {
    this[key] = JsonPrimitive(value)
}

fun MutableMap<String, JsonElement>.put(key: String, value: List<JsonElement>) {
    this[key] = JsonArray(value)
}

fun MutableMap<String, JsonElement>.put(key: String, value: Map<String, JsonElement>) {
    this[key] = JsonObject(value)
}

fun Map<String, JsonElement>.getString(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

fun Map<String, JsonElement>.getInt(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull

fun Map<String, JsonElement>.getLong(key: String): Long? = this[key]?.jsonPrimitive?.longOrNull

fun Map<String, JsonElement>.getBoolean(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull

fun Map<String, JsonElement>.getDouble(key: String): Double? = this[key]?.jsonPrimitive?.let { it.doubleOrNull ?: it.intOrNull?.toDouble() }
