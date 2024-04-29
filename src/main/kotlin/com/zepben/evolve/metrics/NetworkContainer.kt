/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import com.zepben.evolve.cim.iec61970.base.core.Feeder
import com.zepben.evolve.cim.iec61970.base.core.GeographicalRegion
import com.zepben.evolve.cim.iec61970.base.core.SubGeographicalRegion
import com.zepben.evolve.cim.iec61970.base.core.Substation
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.LvFeeder

/**
 * A union type of TotalNetworkContainer (all network processed in a run) and PartialNetworkContainer (network within a equipment container)
 */
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

// Java interop
fun networkContainer(geographicalRegion: GeographicalRegion) = geographicalRegion.toNetworkContainer()
fun networkContainer(subGeographicalRegion: SubGeographicalRegion) = subGeographicalRegion.toNetworkContainer()
fun networkContainer(substation: Substation) = substation.toNetworkContainer()
fun networkContainer(feeder: Feeder) = feeder.toNetworkContainer()
fun networkContainer(lvFeeder: LvFeeder) = lvFeeder.toNetworkContainer()
