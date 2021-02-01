/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks

import com.zepben.evolve.cim.iec61970.base.core.ConnectivityNode
import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.wires.AcLineSegment
import com.zepben.evolve.cim.iec61970.base.wires.Junction
import com.zepben.evolve.cim.iec61970.base.wires.PowerTransformer
import com.zepben.evolve.services.network.NetworkService
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class NetworkCreatorTest {

    val net = NetworkService()

    @Test

    internal fun createLine() {
        val b1 = net.createBus{name="bus1"}
        val b2 = net.createBus{name="bus2"}
        val expected = net.createLine(b1,b2){}
        val result = net.get<AcLineSegment>(expected.mRID)
        assertThat(result, equalTo(expected))
        assertThat(result!!.numTerminals(), equalTo(2))
        assertThat(result.getTerminal(1)!!.connectivityNode, equalTo(b1.getTerminal(1)!!.connectivityNode))
        assertThat(result.getTerminal(2)!!.connectivityNode, equalTo(b2.getTerminal(1)!!.connectivityNode))
    }

    @Test
    internal fun createBusTest() {
        val n = "bus1"
        val obj= net.createBus{name = n; assertThat(this.name, equalTo(n))}
        assertThat(net.get<IdentifiedObject>(obj.mRID), equalTo(obj))
    }

    @Test
    internal fun createEnergySourceTest() {
        val n = "es1"
        val bus = net.createBus {}
        val obj= net.createEnergySource(bus){name = n; assertThat(this.name, equalTo(name))}
        assertThat(net.get<IdentifiedObject>(obj.mRID), equalTo(obj))
       //assertThat(obj.numTerminals(), equalTo(1))
    }


    @Test
    internal fun createConnectivityNodesTest(){
        val cn = net.createConnectivityNode {}
        assertThat(cn, instanceOf(ConnectivityNode::class.java))
        assertThat(net[cn.mRID], equalTo(cn))
    }
    @Test
    internal fun createTransformerTest(){
        val bus1 = net.createBus {}
        val bus2 = net.createBus {}
        val pt = net.createTransformer(bus1, bus2){}
        assertThat(pt,instanceOf(PowerTransformer::class.java))
        assertThat(pt.numEnds(), equalTo(2))
        assertThat(pt.numEnds(), equalTo(pt.numTerminals()))
        assertThat(bus1.getTerminal(1)!!.connectivityNode, equalTo(pt.getTerminal(1)!!.connectivityNode))
        assertThat(bus2.getTerminal(1)!!.connectivityNode, equalTo(pt.getTerminal(2)!!.connectivityNode))
        assertThat(pt.getEnd(1)!!.terminal!!.connectivityNode, equalTo(bus1.getTerminal(1)!!.connectivityNode))
        assertThat(pt.getEnd(2)!!.terminal!!.connectivityNode, equalTo(bus2.getTerminal(1)!!.connectivityNode))
    }
}