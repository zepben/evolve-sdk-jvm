/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks

import com.zepben.evolve.cim.iec61968.common.Location
import com.zepben.evolve.cim.iec61968.common.PositionPoint
import com.zepben.evolve.cim.iec61970.base.core.BaseVoltage
import com.zepben.evolve.cim.iec61970.base.diagramlayout.DiagramObject
import com.zepben.evolve.cim.iec61970.base.diagramlayout.DiagramObjectStyle
import com.zepben.evolve.database.sqlite.DatabaseWriter
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.meta.DataSource
import com.zepben.evolve.services.common.meta.MetadataCollection
import com.zepben.evolve.services.diagram.DiagramService
import com.zepben.evolve.services.network.NetworkService

class SimpleBusBranchNetwork {
    // Create empty network
    val net = NetworkService()
    fun createNetwork(): NetworkService {
        // Create buses
        val bvHV = BaseVoltage().apply { nominalVoltage = 20000 }
        val bvLV = BaseVoltage().apply { nominalVoltage = 400 }
        val b1 = net.createBus(bvHV) { name = "Bus 1" }
        val b2 = net.createBus(bvLV) { name = "Bus 2" }
        val b3 = net.createBus(bvLV) { name = "Bus 3" }

        // Assign Locations to the buses
        val locBus1 = Location().addPoint(PositionPoint(xPosition = 149.12896209173016, yPosition = -35.2782842195893))
        val locBus2 = Location().addPoint(PositionPoint(xPosition = 149.12893605401285, yPosition = -35.278532666462475))
        val locBus3 = Location().addPoint(PositionPoint(xPosition = 149.1286965530321, yPosition = -35.279374652396726))
        b1.apply { location = locBus1 }
        b1.apply { location = locBus2 }
        b1.apply { location = locBus3 }

        // Create bus elements
        net.createEnergySource(bus = b1) { voltageMagnitude = 1.02 * bvHV.nominalVoltage; name = "Grid Connection" }
        net.createLoad(bus = b3) { p = 100000.0; q = 50000.0; name = "Load" }

        // Create branch elements
        net.createTransformer(bus1 = b1, bus2 = b2, ptInfo = net.getAvailablePowerTransformerInfo("0.4 MVA 20/0.4 kV")) { name = "Trafo" }
        net.createLine(bus1 = b2, bus2 = b3) {
            length = 100.0; name = "Line"; perLengthSequenceImpedance = net.getAvailablePerLengthSequenceImpedance("NAYY 4x150 SE")
        }
        return net
    }

    fun createDiagram(): DiagramService {
        return net.createDiagram()
    }
}

fun main(){

    val net = SimpleBusBranchNetwork().createNetwork()
    val diag = SimpleBusBranchNetwork().createDiagram()
    val databaseFile = "cim.db"
    val writer = DatabaseWriter(databaseFile)
    val metaData = MetadataCollection().apply { add(DataSource("Zepben", version = "0.1")) }
    writer.save(metaData, listOf(net, diag))
}

