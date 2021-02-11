/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.services.common.BaseService
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test



class SimpleBusBranchTest {
    val net = SimpleBusBranchNetwork().getNetworkService()
    //TODO: Test for getDiagramService

    @Test
    internal fun basicServiceCreated() {
        assertThat(net, instanceOf(BaseService::class.java))
    }

    @Test
    internal fun notNullObjects() {
        assertThat(net.setOf<IdentifiedObject>(), notNullValue())
        assertThat(net.setOf<ConnectivityNode>(), notNullValue())
        assertThat(net.setOf<ConductingEquipment>(), notNullValue())
        assertThat(net.setOf<Terminal>(), notNullValue())
        assertThat(net.setOf<Junction>(), notNullValue())
        assertThat(net.setOf<PowerTransformer>(), notNullValue())
        assertThat(net.setOf<AcLineSegment>(), notNullValue())
        assertThat(net.setOf<EnergySource>(), notNullValue())
        assertThat(net.setOf<EnergyConsumer>(), notNullValue())
        assertThat(net.setOf<BaseVoltage>(), notNullValue())
    }
    @Test
    internal fun numberOfObject(){
        assertThat(net.setOf<PowerTransformer>().size, equalTo(1))
        assertThat(net.setOf<AcLineSegment>().size, equalTo(1))
        assertThat(net.setOf<Junction>().size, equalTo(3))
        assertThat(net.setOf<EnergySource>().size, equalTo(1))
        assertThat(net.setOf<EnergyConsumer>().size, equalTo(1))
        assertThat(net.setOf<BaseVoltage>().size, equalTo(2))
        assertThat(net.setOf<ConnectivityNode>().size, equalTo(3))
    }
}