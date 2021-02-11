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
import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.Equipment
import com.zepben.evolve.cim.iec61970.base.core.Feeder
import com.zepben.evolve.cim.iec61970.base.diagramlayout.*
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.database.sqlite.DatabaseWriter
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.meta.DataSource
import com.zepben.evolve.services.common.meta.MetadataCollection
import com.zepben.evolve.services.diagram.DiagramService
import com.zepben.evolve.services.network.NetworkService

class SimpleBusBranchNetwork {
    // Create empty network
    val net = NetworkService()
    val diag = DiagramService()

    init {createNetwork()}

    fun getNetworkService(): NetworkService {return net}

    fun getDiagramService(): DiagramService {return diag}

    fun writeDb(dbpath: String){
        val writer = DatabaseWriter(dbpath)
        val metaData = MetadataCollection().apply { add(DataSource("Zepben", version = "0.1")) }
        writer.save(metaData, listOf(net, diag))
    }

    private fun createNetwork() {
        // Create BaseVoltages
        val bvHV = BaseVoltage(mRID= "20kV").apply {nominalVoltage = 20000; name = "20kV"}
        val bvLV = BaseVoltage(mRID= "415V").apply { nominalVoltage = 400; name = "415V"}
        net.add(bvHV)
        net.add(bvLV)
        // Create Locations for buses
        val point1 = PositionPoint(xPosition = 149.12896209173016, yPosition = -35.2782842195893)
        val point2 = PositionPoint(xPosition = 149.12893605401285, yPosition = -35.278532666462475)
        val point3 = PositionPoint(xPosition = 149.1286965530321, yPosition = -35.279374652396726)
        val locBus1 = Location().addPoint(point1)
        val locBus2 = Location().addPoint(point2)
        val locBus3 = Location().addPoint(point3)
        net.add(locBus1)
        net.add(locBus2)
        net.add(locBus3)
        // Create buses
        val b1 = net.createBus(bvHV) { name = "Bus 1"; location = locBus1}
        val b2 = net.createBus(bvLV) { name = "Bus 2"; location = locBus2}
        val b3 = net.createBus(bvLV) { name = "Bus 3"; location = locBus3}
        // Create EnergySource
        val energySource = net.createEnergySource(bus = b1) { voltageMagnitude = 1.02 * bvHV.nominalVoltage; name = "Grid Connection"; location = locBus1}
        // Create Feeder
        // val fdr = Feeder().apply { normalHeadTerminal = energySource.getTerminal(1)}
        // net.add(fdr)
        // Create Load
        net.createLoad(bus = b3) { p = 100000.0; q = 50000.0; name = "Load"; location = locBus3}
        // Create location for the PowerTransformer
        val locTx = Location().addPoint(PositionPoint(xPosition =  149.128891789997, yPosition =  -35.27844728544802))
        net.add(locTx)
        // Create Transformer
        val tx = net.createTransformer(bus1 = b1, bus2 = b2, ptInfo = net.getAvailablePowerTransformerInfo("0.4 MVA 20/0.4 kV"))
        {name = "Trafo"; location=locTx}

        // Create location for the Line
        val locLine = Location().addPoint(point2).addPoint(point2)
        net.add(locLine)
        // Create Line
        val line = net.createLine(bus1 = b2, bus2 = b3) {
            length = 100.0;
            name = "Line";
            perLengthSequenceImpedance = net.getAvailablePerLengthSequenceImpedance("NAYY 4x150 SE")
            location = locLine
            baseVoltage = bvLV
        }

        // Create DiagramObject for branch elements
        val di = Diagram().apply { diagramStyle = DiagramStyle.GEOGRAPHIC; numDiagramObjects =1}
        diag.add(di)
        val diagramObject = DiagramObject(mRID = line.mRID + "-do").apply { identifiedObjectMRID = line.mRID; style = DiagramObjectStyle.CONDUCTOR_LV; diagram = di}
        diagramObject.addPoint(DiagramObjectPoint(xPosition = point2.xPosition, yPosition = point2.yPosition))
        diagramObject.addPoint(DiagramObjectPoint(xPosition = point3.xPosition, yPosition = point3.yPosition))
        line.apply {numDiagramObjects = 1}
        di.addDiagramObject(diagramObject)
        diag.add(diagramObject)
    }

    private fun addDiagramObjects(){
        val list = net.listOf<ConductingEquipment>()
        diag.add(DiagramObject().apply { style = DiagramObjectStyle.USAGE_POINT })
        list.forEach{
            val diaObj =  when (it){
                is Junction -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.JUNCTION}
                is PowerTransformer -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.DIST_TRANSFORMER}
                is EnergySource -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.ENERGY_SOURCE}
                is EnergyConsumer -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.USAGE_POINT}
                else -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.JUNCTION}
            }
            diag.add(diaObj)
        }
    }
}

fun main(){
    val net = SimpleBusBranchNetwork()
    net.writeDb("F:\\Data\\ewb\\zepben\\2021-02-11\\2021-02-11-network-model.sqlite")
    //TODO: Equipments Bus 2, Bus 3, Trafo and ACLines are not appearing in the EWB map.
}

