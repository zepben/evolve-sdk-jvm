/*
 * Copyright Zeppelin Bend Pty Ltd (Zepben). The use of this file and its contents requires explicit permission from Zepben.
 */

package com.zepben.evolve.cim.geojson

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive


@Serializable
enum class GeometryType {
    LineString,
    Point,
    Polygon
}

/**
 * Note: GeoJson standard indicates that you must always have a Geometry, it must have a type, however coordinates can be an empty array.
 * We extend this for simplicity to allow null types, coordinates, and also a null Geometry on a Feature
 */
@Serializable
@SerialName("geometry")
data class Geometry(val type: GeometryType?, val coordinates: List<JsonElement>?)

val NULL_GEOMETRY = Geometry(GeometryType.Point, emptyList())