/*
 * Copyright Zeppelin Bend Pty Ltd (Zepben). The use of this file and its contents requires explicit permission from Zepben.
 */

package com.zepben.evolve.cim.geojson

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class GeometryType {
    LineString,
    Point
}

@Serializable
@SerialName("geometry")
data class Geometry(val type: GeometryType, val coordinates: List<List<Double>>) {
    init {
        if (type == GeometryType.Point)
            require(coordinates.size == 1) { "Point geometries must only have 1 set of coordinates."}
    }
}
