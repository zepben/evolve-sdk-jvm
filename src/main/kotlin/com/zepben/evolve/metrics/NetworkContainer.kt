package com.zepben.evolve.metrics

import com.zepben.evolve.cim.iec61970.base.core.Feeder
import com.zepben.evolve.cim.iec61970.base.core.GeographicalRegion
import com.zepben.evolve.cim.iec61970.base.core.SubGeographicalRegion
import com.zepben.evolve.cim.iec61970.base.core.Substation
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.LvFeeder

sealed interface NetworkContainer

data class PartialNetworkContainer(
    val level: NetworkLevel,
    val mRID: String,
    val name: String
) : NetworkContainer

data object TotalNetworkContainer : NetworkContainer

enum class NetworkLevel {
    GeographicalRegion,
    SubGeographicalRegion,
    Substation,
    Feeder,
    LvFeeder
}

fun GeographicalRegion.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(NetworkLevel.GeographicalRegion, mRID, name)
fun SubGeographicalRegion.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(NetworkLevel.SubGeographicalRegion, mRID, name)
fun Substation.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(NetworkLevel.Substation, mRID, name)
fun Feeder.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(NetworkLevel.Feeder, mRID, name)
fun LvFeeder.toNetworkContainer(): PartialNetworkContainer =
    PartialNetworkContainer(NetworkLevel.LvFeeder, mRID, name)
