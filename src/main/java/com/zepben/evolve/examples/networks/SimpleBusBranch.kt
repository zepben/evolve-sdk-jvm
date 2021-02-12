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
        val point1 = PositionPoint(xPosition = 149.12791965570293, yPosition = -35.277592101000934)
        val point2 = PositionPoint(xPosition =  149.12779472660375, yPosition = -35.278183862759285)
        val loc1 = Location().addPoint(point1)
        val loc2 = Location().addPoint(point2)
        net.add(loc1)
        net.add(loc2)
        // Create buses
        val b1 = net.createBus(bvHV) { name = "Bus 1"; location = loc1}
        val b2 = net.createBus(bvLV) { name = "Bus 2"; location = loc1}
        val b3 = net.createBus(bvLV) { name = "Bus 3"; location = loc2}
        // Create EnergySource
        val energySource = net.createEnergySource(bus = b1) { voltageMagnitude = 1.02 * bvHV.nominalVoltage; name = "Grid Connection"; location = loc1}
        // Create Feeder
        // val fdr = Feeder().apply { normalHeadTerminal = energySource.getTerminal(1)}
        // net.add(fdr)
        // Create Load
        net.createLoad(bus = b3) { p = 100000.0; q = 50000.0; name = "Load"; location = loc2}
        // Create Transformer
        val tx = net.createTransformer(bus1 = b1, bus2 = b2, ptInfo = net.getAvailablePowerTransformerInfo("0.4 MVA 20/0.4 kV"))
        {name = "Trafo"; location=loc1}
        // Create location for the Line
        val locLine = Location().addPoint(point1).addPoint(point2)
        net.add(locLine)
        // Create Line
        val line = net.createLine(bus1 = b2, bus2 = b3) {
            length = 100.0;
            name = "Line";
            perLengthSequenceImpedance = net.getAvailablePerLengthSequenceImpedance("NAYY 4x150 SE")
            location = locLine
            baseVoltage = bvLV
        }
    }

    private fun addDiagramObjects(){
        val di = Diagram().apply { diagramStyle = DiagramStyle.GEOGRAPHIC; numDiagramObjects =1}
        diag.add(di)
        // Add DiagramObject for ConductingEquipments
        val list = net.listOf<ConductingEquipment>()
        diag.add(DiagramObject().apply { style = DiagramObjectStyle.USAGE_POINT })
        list.forEach{
            val diagramObject =  when (it){
                is AcLineSegment -> addDiagramObjectstoAcLineSegs(it, di)
                is Junction -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.JUNCTION}
                is PowerTransformer -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.DIST_TRANSFORMER}
                is EnergySource -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.ENERGY_SOURCE}
                is EnergyConsumer -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.USAGE_POINT}
                else -> DiagramObject().apply {identifiedObjectMRID = it.mRID; style = DiagramObjectStyle.JUNCTION}
            }
            di.addDiagramObject(diagramObject)
            diag.add(diagramObject)
        }
    }
    private fun addDiagramObjectstoAcLineSegs(acLineSegment: AcLineSegment, di: Diagram): DiagramObject{
        // Create DiagramObject for AcLineSegments
        val diagramObject = DiagramObject(mRID = acLineSegment.mRID + "-do").apply { identifiedObjectMRID = acLineSegment.mRID; style = DiagramObjectStyle.CONDUCTOR_LV; diagram = di}
        val point1 = acLineSegment.location!!.getPoint(0)!!
        val point2 = acLineSegment.location!!.getPoint(1)!!
        diagramObject.addPoint(DiagramObjectPoint(xPosition = point1.xPosition, yPosition = point1.yPosition))
        diagramObject.addPoint(DiagramObjectPoint(xPosition = point2.xPosition, yPosition = point2.yPosition))
        return diagramObject
    }
}

fun main(){
    val net = SimpleBusBranchNetwork()
    net.writeDb("F:\\Data\\ewb\\zepben\\2021-02-11\\2021-02-11-network-model.sqlite")
}

