/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks

import com.zepben.evolve.cim.iec61968.assetinfo.OverheadWireInfo
import com.zepben.evolve.cim.iec61968.assetinfo.PowerTransformerInfo
import com.zepben.evolve.cim.iec61970.base.core.BaseVoltage
import com.zepben.evolve.cim.iec61970.base.core.ConnectivityNode
import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.services.network.NetworkService
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class NetworkCreatorTest {

    val net = NetworkService()
    val bv = BaseVoltage()
    val bus1 = net.createBus(bv){name="bus1"}
    val bus2 = net.createBus(bv){name="bus2"}


    @Test
    internal fun  createEnergyConsumerTest(){
        val expected = net.createEnergyConsumer(bus1){ p = 1.0; q = 1.0}
        val result = net.get<EnergyConsumer>(expected.mRID)
        assertThat(result, equalTo(expected))
        assertThat(result!!.numTerminals(), equalTo(1))
        assertThat(result.getTerminal(1)!!.connectivityNode, equalTo(bus1.getTerminal(1)!!.connectivityNode))
    }

    @Test
    internal fun createLineTest() {
        val expected = net.createLine(bus1,bus2){}
        val result = net.get<AcLineSegment>(expected.mRID)
        assertThat(result, equalTo(expected))
        assertThat(result!!.numTerminals(), equalTo(2))
        assertThat(result.getTerminal(1)!!.connectivityNode, equalTo(bus1.getTerminal(1)!!.connectivityNode))
        assertThat(result.getTerminal(2)!!.connectivityNode, equalTo(bus2.getTerminal(1)!!.connectivityNode))
    }

    @Test
    internal fun createBusTest() {
        val n = "bus1"
        val obj= net.createBus(bv){name = n; assertThat(this.name, equalTo(n))}
        assertThat(net.get<IdentifiedObject>(obj.mRID), equalTo(obj))
    }

    @Test
    internal fun createEnergySourceTest() {
        val n = "es1"
        val bus = net.createBus(bv){}
        val obj= net.createEnergySource(bus){name = n; assertThat(this.name, equalTo(name))}
        assertThat(net.get<IdentifiedObject>(obj.mRID), equalTo(obj))
    }


    @Test
    internal fun createConnectivityNodesTest(){
        val cn = net.createConnectivityNode {}
        assertThat(cn, instanceOf(ConnectivityNode::class.java))
        assertThat(net[cn.mRID], equalTo(cn))
    }
    @Test
    internal fun createTransformerTest(){
        val pt = net.createTransformer(bus1, bus2, ptInfo = net.getAvailablePowerTransformerInfo("0.4 MVA 20/0.4 kV")){}
        assertThat(pt,instanceOf(PowerTransformer::class.java))
        assertThat(pt.numEnds(), equalTo(2))
        assertThat(pt.numEnds(), equalTo(pt.numTerminals()))
        assertThat(bus1.getTerminal(1)!!.connectivityNode, equalTo(pt.getTerminal(1)!!.connectivityNode))
        assertThat(bus2.getTerminal(1)!!.connectivityNode, equalTo(pt.getTerminal(2)!!.connectivityNode))
        assertThat(pt.getEnd(1)!!.terminal!!.connectivityNode, equalTo(bus1.getTerminal(1)!!.connectivityNode))
        assertThat(pt.getEnd(2)!!.terminal!!.connectivityNode, equalTo(bus2.getTerminal(1)!!.connectivityNode))
    }

    @Test
    internal fun getAvailablePowerTransformerInfoTest() {
        val info = net.getAvailablePowerTransformerInfo("0.4 MVA 20/0.4 kV")
        assertThat(info, instanceOf(PowerTransformerInfo::class.java))
    }

    @Test
    internal fun getAvailableWireInfoTest() {
        val info = net.getAvailableWireInfo("N2XS(FL)2Y 1x300 RM/35 64/110 kV" )
        assertThat(info, instanceOf(OverheadWireInfo::class.java))
        assertThat(info.ratedCurrent, equalTo(366))
    }

    @Test
    internal fun etAvailablePerLengthSequenceImpedanceTest() {
        val info = net.getAvailablePerLengthSequenceImpedance("N2XS(FL)2Y 1x300 RM/35 64/110 kV" )
        assertThat(info, instanceOf(PerLengthSequenceImpedance::class.java))
        assertThat(info.r, equalTo(0.060/1000))
    }

}