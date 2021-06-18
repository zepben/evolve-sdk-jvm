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

    fun isLvTransformer(lvThreshold: Int = 1000): Boolean {
        if (properties.getString("class") == FeatureClass.PowerTransformer.name) {
            return properties.getMapList("ends")?.let { ends -> ends.any { end -> end.getInt("ratedS") ?: Int.MAX_VALUE <= lvThreshold } }
                ?: (properties.getInt("nominalVoltage") ?: Int.MAX_VALUE <= lvThreshold)
        }
        return false
    }

    fun isLvVoltage(lvThreshold: Int = 1000): Boolean = properties.getInt("nominalVoltage") ?: Int.MAX_VALUE <= lvThreshold

    operator fun get(key: String): Any? = properties[key]

    fun put(key: String, value: String) {
        properties[key] = JsonPrimitive(value)
    }

    fun put(key: String, value: Number) {
        properties[key] = JsonPrimitive(value)
    }

    fun put(key: String, value: Boolean) {
        properties[key] = JsonPrimitive(value)
    }

    fun put(key: String, value: List<JsonElement>) {
        properties[key] = JsonArray(value)
    }

    fun put(key: String, value: Map<String, JsonElement>) {
        properties[key] = JsonObject(value)
    }

//    inline fun <reified T> put(key: String, value: T) {
//        properties[key] = Json.encodeToJsonElement(kotlinx.serialization.serializer(), value)
//    }

    fun getIntList(key: String): List<Int>? = properties.getIntList(key)

    fun getStringList(key: String): List<String>? = properties.getStringList(key)

    fun getMapList(key: String): MutableList<MutableMap<String, JsonElement>>? = properties.getMapList(key)

    fun getString(key: String): String? = properties.getString(key)

    fun getInt(key: String): Int? = properties.getInt(key)

    fun getBoolean(key: String): Boolean? = properties.getBoolean(key)

    fun getDouble(key: String): Double = properties.getDouble(key)

}
