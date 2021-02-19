/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.diagramlayout.Diagram
import com.zepben.evolve.cim.iec61970.base.diagramlayout.DiagramObject
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.diagram.DiagramService
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.tracing.traversals.BasicTraversalTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test



class SimpleBusBranchTest {
    val networkService = SimpleBusBranchNetwork().getNetwork()
    val diagramService = SimpleBusBranchNetwork().getDiagramService()

    @Test
    internal fun basicServicesCreated() {
        assertThat(networkService, instanceOf(BaseService::class.java))
        assertThat(diagramService, instanceOf(BaseService::class.java))
    }

    @Test
    internal fun notNullObjects() {
        assertThat(networkService.setOf<IdentifiedObject>(), notNullValue())
        assertThat(networkService.setOf<ConnectivityNode>(), notNullValue())
        assertThat(networkService.setOf<ConductingEquipment>(), notNullValue())
        assertThat(networkService.setOf<Terminal>(), notNullValue())
        assertThat(networkService.setOf<Junction>(), notNullValue())
        assertThat(networkService.setOf<PowerTransformer>(), notNullValue())
        assertThat(networkService.setOf<AcLineSegment>(), notNullValue())
        assertThat(networkService.setOf<EnergySource>(), notNullValue())
        assertThat(networkService.setOf<EnergyConsumer>(), notNullValue())
        assertThat(networkService.setOf<BaseVoltage>(), notNullValue())
    }
    @Test
    internal fun numberOfObject(){
        assertThat(networkService.setOf<PowerTransformer>().size, equalTo(1))
        assertThat(networkService.setOf<AcLineSegment>().size, equalTo(1))
        assertThat(networkService.setOf<Junction>().size, equalTo(3))
        assertThat(networkService.setOf<EnergySource>().size, equalTo(1))
        assertThat(networkService.setOf<EnergyConsumer>().size, equalTo(1))
        assertThat(networkService.setOf<BaseVoltage>().size, equalTo(2))
        assertThat(networkService.setOf<ConnectivityNode>().size, equalTo(3))
        assertThat(networkService.setOf<Terminal>().size, equalTo(9))
    }

    @Test
    internal fun createDiagramServiceTest(){
        assertThat(diagramService.setOf<DiagramObject>().size, equalTo(7))
        assertThat(diagramService.setOf<Diagram>().size, equalTo(1))
    }
}