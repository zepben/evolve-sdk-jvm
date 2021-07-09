/*
 * Copyright Zeppelin Bend Pty Ltd (Zepben). The use of this file and its contents requires explicit permission from Zepben.
 */

package com.zepben.evolve.cim.geojson

import com.zepben.evolve.services.network.translator.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class Feature(
    val id: String,
    val type: FeatureType = FeatureType.Feature,
    val geometry: Geometry? = null,
    val properties: MutableMap<String, JsonElement> = mutableMapOf()
) {

    operator fun get(key: String): Any? = properties[key]

    fun put(key: String, value: String?) {
        if (!value.isNullOrEmpty())
            properties[key] = JsonPrimitive(value)
    }

    fun put(key: String, value: Number) {
        properties[key] = JsonPrimitive(value)
    }

    fun put(key: String, value: Boolean) {
        properties[key] = JsonPrimitive(value)
    }

    fun put(key: String, value: List<JsonElement>) {
        if (value.isNotEmpty())
            properties[key] = JsonArray(value)
    }

    fun put(key: String, value: Map<String, JsonElement>) {
        if (value.isNotEmpty())
            properties[key] = JsonObject(value)
    }

    fun getIntList(key: String): List<Int>? = properties.getIntList(key)

    fun getList(key: String): List<JsonElement>? = properties.getList(key)

    fun getStringList(key: String): List<String>? = properties.getStringList(key)

    fun getMap(key: String): Map<String, JsonElement>? = properties.getMap(key)

    fun getMapList(key: String): List<Map<String, JsonElement>>? = properties.getMapList(key)

    fun getString(key: String): String? = properties.getString(key)

    fun getInt(key: String): Int? = properties.getInt(key)

    fun getLong(key: String): Long? = properties.getLong(key)

    fun getBoolean(key: String): Boolean? = properties.getBoolean(key)

    fun getDouble(key: String): Double? = properties.getDouble(key)

}
