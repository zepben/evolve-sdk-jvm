/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.streaming.put

import com.zepben.evolve.cim.iec61968.assetinfo.CableInfo
import com.zepben.evolve.services.customer.CustomerService
import com.zepben.evolve.services.diagram.DiagramService
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.streaming.grpc.ConnectionConfig
import com.zepben.evolve.streaming.grpc.GrpcChannelFactory
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@Disabled
internal class EndToEndTest {

    val channel = GrpcChannelFactory.create(ConnectionConfig("localhost", 50051))

    @Test
    internal fun `connects and sends to a cimcap server`() {
        try {
            val networkService = NetworkService()
            val customerService = CustomerService()
            val diagramService = DiagramService()
            populateNetworkObjects(networkService)

            NetworkProducerClient(channel).send(networkService)
            CustomerProducerClient(channel).send(customerService)
            DiagramProducerClient(channel).send(diagramService)
        } finally {
            channel.shutdown()
            channel.awaitTermination(5, TimeUnit.SECONDS)
        }
    }

    private fun populateNetworkObjects(networkService: NetworkService) {
        networkService.add(CableInfo("CableInfo1"))
//        networkService.add(OverheadWireInfo("OverheadWireInfo1"))
//        networkService.add(AssetOwner("AssetOwner1"))
//        networkService.add(Location("Location1"))
//        networkService.add(Organisation("Organisation1"))
//        networkService.add(Meter("Meter1"))
//        networkService.add(UsagePoint("UsagePoint1"))
//        networkService.add(OperationalRestriction("OperationalRestriction1"))
//        networkService.add(FaultIndicator("FaultIndicator1"))
//        networkService.add(BaseVoltage("BaseVoltage1"))
//        networkService.add(ConnectivityNode("ConnectivityNode1"))
//        networkService.add(Feeder("Feeder1"))
//        networkService.add(GeographicalRegion("GeographicalRegion1"))
//        networkService.add(Site("Site1"))
//        networkService.add(SubGeographicalRegion("SubGeographicalRegion1"))
//        networkService.add(Substation("Substation1"))
//        networkService.add(Terminal("Terminal1"))
//        networkService.add(AcLineSegment("AcLineSegment1"))
//        networkService.add(Breaker("Breaker1"))
//        networkService.add(Disconnector("Disconnector1"))
//        networkService.add(EnergyConsumer("EnergyConsumer1"))
//        networkService.add(EnergyConsumerPhase("EnergyConsumerPhase1"))
//        networkService.add(EnergySource("EnergySource1"))
//        networkService.add(EnergySourcePhase("EnergySourcePhase1"))
//        networkService.add(Fuse("Fuse1"))
//        networkService.add(Jumper("Jumper1"))
//        networkService.add(Junction("Junction1"))
//        networkService.add(LinearShuntCompensator("LinearShuntCompensator1"))
//        networkService.add(PerLengthSequenceImpedance("PerLengthSequenceImpedance1"))
//        networkService.add(PowerTransformer("PowerTransformer1"))
//        networkService.add(PowerTransformerEnd("PowerTransformerEnd1"))
//        networkService.add(RatioTapChanger("RatioTapChanger1"))
//        networkService.add(Recloser("Recloser1"))
    }
}