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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

internal class NetworkContainerTest {

    @Test
    internal fun `extension functions`() {
        val fromGeoRegion = GeographicalRegion("GR").apply { name = "geoRegion" }.toNetworkContainer()
        val fromSubGeoRegion = SubGeographicalRegion("SGR").apply { name = "subGeoRegion" }.toNetworkContainer()
        val fromSubstation = Substation("SS").apply { name = "substation" }.toNetworkContainer()
        val fromFeeder = Feeder("FDR").apply { name = "feeder" }.toNetworkContainer()
        val fromLvFeeder = LvFeeder("LVF").apply { name = "lvFeeder" }.toNetworkContainer()

        assertThat(fromGeoRegion, equalTo(PartialNetworkContainer(NetworkLevel.GeographicalRegion, "GR", "geoRegion")))
        assertThat(fromSubGeoRegion, equalTo(PartialNetworkContainer(NetworkLevel.SubGeographicalRegion, "SGR", "subGeoRegion")))
        assertThat(fromSubstation, equalTo(PartialNetworkContainer(NetworkLevel.Substation, "SS", "substation")))
        assertThat(fromFeeder, equalTo(PartialNetworkContainer(NetworkLevel.Feeder, "FDR", "feeder")))
        assertThat(fromLvFeeder, equalTo(PartialNetworkContainer(NetworkLevel.LvFeeder, "LVF", "lvFeeder")))
    }

    @Test
    internal fun `accessor coverage`() {
        val partialNetworkContainer = PartialNetworkContainer(NetworkLevel.GeographicalRegion, "GR", "geoRegion")
        assertThat(partialNetworkContainer.level, equalTo(NetworkLevel.GeographicalRegion))
        assertThat(partialNetworkContainer.mRID, equalTo("GR"))
        assertThat(partialNetworkContainer.name, equalTo("geoRegion"))
    }

}
