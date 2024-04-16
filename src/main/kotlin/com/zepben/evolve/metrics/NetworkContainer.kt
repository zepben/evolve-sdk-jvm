package com.zepben.evolve.metrics

import com.zepben.evolve.cim.iec61970.base.core.Feeder
import com.zepben.evolve.cim.iec61970.base.core.GeographicalRegion
import com.zepben.evolve.cim.iec61970.base.core.SubGeographicalRegion
import com.zepben.evolve.cim.iec61970.base.core.Substation
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.LvFeeder

sealed interface NetworkContainer

data class PartialNetworkContainer(
    val level: HierarchyLevel,
    val mRID: String,
    val name: String
) : NetworkContainer

data object TotalNetworkContainer : NetworkContainer

enum class HierarchyLevel {
    GeographicalRegion,
    SubgeographicalRegion,
    Substation,
    Feeder,
    LvFeeder
}

fun GeographicalRegion.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(HierarchyLevel.GeographicalRegion, mRID, name)
fun SubGeographicalRegion.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(HierarchyLevel.SubgeographicalRegion, mRID, name)
fun Substation.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(HierarchyLevel.Substation, mRID, name)
fun Feeder.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(HierarchyLevel.Feeder, mRID, name)
fun LvFeeder.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(HierarchyLevel.LvFeeder, mRID, name)
