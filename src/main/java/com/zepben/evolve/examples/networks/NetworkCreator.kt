/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks


import com.zepben.evolve.cim.iec61968.assetinfo.PowerTransformerInfo
import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.ConnectivityNode
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.services.network.NetworkService



fun NetworkService.createBus(numTerminals: Int = 0, init: Junction.() -> Unit): Junction = create(::Junction, numTerminals, init)
// TODO: Figure out how to add Voltage to Buses - Looks like we need to add topologicalNode to support the relationship to BaseVoltage. Meanwhile using Junction.
fun NetworkService.createEnergySource(numTerminals: Int = 1, init: EnergySource.() -> Unit): EnergySource = create(::EnergySource, numTerminals,  init)
fun NetworkService.createConnectivityNode(init: ConnectivityNode.()-> Unit): ConnectivityNode{
    val cn = ConnectivityNode().apply(init)
    this.add(cn)
    return cn
}
fun NetworkService.createTransformer(numEnds: Int = 2, info: String = "25 MVA 110/20 kV", init: PowerTransformer.() -> Unit): PowerTransformer{
    val pt = PowerTransformer().apply(init)
    this.add(pt)
    for (i in 1..numEnds) {
        val end = PowerTransformerEnd().apply {powerTransformer = pt}
        this.tryAdd(end)
        pt.addEnd(end)
    }
    pt.createTerminals(numEnds, this)
    val ptInfo = getAvailableTransformerInfo(info)
    pt.apply{assetInfo=ptInfo}
    return pt
}

fun NetworkService.createLine(bus1:  Junction, bus2: Junction, init: AcLineSegment.() -> Unit): AcLineSegment{
    val acls = AcLineSegment().apply(init)
    acls.createTerminals(2,this)
    val terminalBus1 = Terminal().apply { conductingEquipment = bus1 }
    val terminalBus2 = Terminal().apply { conductingEquipment = bus2 }
    bus1.addTerminal(terminalBus1)
    bus2.addTerminal(terminalBus2)
    this.tryAdd(terminalBus1)
    this.tryAdd(terminalBus2)
    this.tryAdd(acls)
    this.connect(acls.getTerminal(1)!!, terminalBus1)
    this.connect(acls.getTerminal(2)!!, terminalBus2)
    return acls
}

fun getAvailableTransformerInfo(mrid: String): PowerTransformerInfo
{
    val list = mutableListOf<PowerTransformerInfo>()
    list.add(PowerTransformerInfo("25 MVA 110/20 kV"))
    list.add(PowerTransformerInfo("0.63 MVA 10/0.4 kV"))
    return list.find { it.mRID == mrid}!!
}


    /*  {
       "i0_percent": 0.07,
       "pfe_kw": 14,
       "vkr_percent": 0.41,
       "sn_mva": 25,
       "vn_lv_kv": 20.0,
       "vn_hv_kv": 110.0,
       "vk_percent": 12,
       "shift_degree": 150,
       "vector_group": "YNd5",
       "tap_side": "hv",
       "tap_neutral": 0,
       "tap_min": -9,
       "tap_max": 9,
       "tap_step_degree": 0,
       "tap_step_percent": 1.5,
       "tap_phase_shifter": False}


       // 0.63 MVA 10/0.4 kV Trafo Union wnr
       "0.63 MVA 10/0.4 kV" to PowerTransformerInfo().apply {  }
       {"sn_mva": 0.63,
           "vn_hv_kv": 10,
           "vn_lv_kv": 0.4,
           "vk_percent": 4,
           "vkr_percent": 1.0794,
           "pfe_kw": 1.18,
           "i0_percent": 0.1873,
           "shift_degree": 150,
           "vector_group": "Dyn5",
           "tap_side": "hv",
           "tap_neutral": 0,
           "tap_min": -2,
           "tap_max": 2,
           "tap_step_degree": 0,
           "tap_step_percent": 2.5,
           "tap_phase_shifter": False}*/



private fun <T : ConductingEquipment> NetworkService.create(creator: () -> T, numTerminals: Int = 1, init: T.() -> Unit): T {
    val obj = creator().apply { createTerminals(numTerminals, NetworkService()) }.apply(init)
    this.tryAdd(obj)
    return obj
}

private fun ConductingEquipment.createTerminals(num: Int, net: NetworkService) {
    for (i in 1..num) {
        val terminal = Terminal()
        net.tryAdd(terminal)
        terminal.conductingEquipment = this
        addTerminal(terminal)
    }
}

