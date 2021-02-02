/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.example.networks

import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.wires.EnergySource
import com.zepben.evolve.cim.iec61970.base.wires.Junction
import com.zepben.evolve.cim.iec61970.base.wires.PowerTransformer
import com.zepben.evolve.examples.networks.simpleNetwork
import com.zepben.evolve.services.common.BaseService
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class SimpleNetworkTest {
    val net = simpleNetwork()

    @Test
    internal fun basicServiceCreated() {
        assertThat(net, instanceOf(BaseService::class.java))
    }

    @Test
    internal fun notNullObjects(){
        assertThat(net.setOf<IdentifiedObject>(), notNullValue())
        assertThat(net.setOf<ConnectivityNode>(), notNullValue())
        assertThat(net.setOf<ConductingEquipment>(), notNullValue())
        assertThat(net.setOf<Terminal>(), notNullValue())
        assertThat(net.setOf<Junction>(), notNullValue())
    }

    @Test
    internal fun createTransformerInfo(){
        assertThat(net.setOf<PowerTransformer>(), notNullValue())
        assertThat(net.setOf<PowerTransformer>().size, equalTo(1))
        val trafo = net.setOf<PowerTransformer>().find {it.name == "110kV/20kV transformer"}!!
        assertThat(trafo.assetInfo!!.mRID, equalTo("25 MVA 110/20 kV"))
    }

    @Test
    internal fun externalGrid(){
        assertThat(net.setOf<EnergySource>(), notNullValue())
        // TODO: Finish this test where we can make a diffrence between EnergySource and EquivalentNetwork.
        //net.setOf<EnergySource>().forEach { assertThat(it.voltageAngle, equalTo(0.8726646259971648))}
        //net.setOf<EnergySource>().forEach { assertThat(it.voltageMagnitude, equalTo(110000*1.02))}
    }
}