package com.zepben.evolve.cim.geojson

import kotlinx.serialization.Serializable

@Serializable
data class FeatureCollection(val type: FeatureType = FeatureType.FeatureCollection, val features: List<Feature>)
