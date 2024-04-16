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
    internal fun extensionFunctions() {
        val fromGeoRegion = GeographicalRegion("GR").apply { name = "geoRegion" }.toNetworkContainer()
        val fromSubGeoRegion = SubGeographicalRegion("SGR").apply { name = "subGeoRegion" }.toNetworkContainer()
        val fromSubstation = Substation("SS").apply { name = "substation" }.toNetworkContainer()
        val fromFeeder = Feeder("FDR").apply { name = "feeder" }.toNetworkContainer()
        val fromLvFeeder = LvFeeder("LVF").apply { name = "lvFeeder" }.toNetworkContainer()

        assertThat(fromGeoRegion, equalTo(PartialNetworkContainer(HierarchyLevel.GeographicalRegion, "GR", "geoRegion")))
        assertThat(fromSubGeoRegion, equalTo(PartialNetworkContainer(HierarchyLevel.SubGeographicalRegion, "SGR", "subGeoRegion")))
        assertThat(fromSubstation, equalTo(PartialNetworkContainer(HierarchyLevel.Substation, "SS", "substation")))
        assertThat(fromFeeder, equalTo(PartialNetworkContainer(HierarchyLevel.Feeder, "FDR", "feeder")))
        assertThat(fromLvFeeder, equalTo(PartialNetworkContainer(HierarchyLevel.LvFeeder, "LVF", "lvFeeder")))
    }

}