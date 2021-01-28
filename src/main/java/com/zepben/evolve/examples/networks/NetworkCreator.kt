/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks


import com.zepben.evolve.cim.iec61968.assetinfo.CableInfo
import com.zepben.evolve.cim.iec61968.assetinfo.OverheadWireInfo
import com.zepben.evolve.cim.iec61968.assetinfo.PowerTransformerInfo
import com.zepben.evolve.cim.iec61968.assetinfo.WireInfo
import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.ConnectivityNode
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.services.network.NetworkService
import com.zepben.protobuf.np.CreateTransformerEndInfoResponse


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

fun NetworkService.createLine(bus1:  Junction, bus2: Junction, std_type: String = "N2XS(FL)2Y 1x300 RM/35 64/110 kV",
                              init: AcLineSegment.() -> Unit): AcLineSegment{
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
    return acls.apply{assetInfo = getAvailableLineStdTypes(std_type)}
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

private fun getAvailableTransformerInfo(mrid: String): PowerTransformerInfo
{
    val list = mutableListOf<PowerTransformerInfo>()
    list.add(PowerTransformerInfo("25 MVA 110/20 kV"))
    list.add(PowerTransformerInfo("0.63 MVA 10/0.4 kV"))
    return list.find { it.mRID == mrid}!!
}

/* linetypes = {
        # Cables, all from S.744, Heuck: Elektrische Energieversorgung - Vierweg+Teubner 2013
        # additional MV cables from Werth: Netzberechnung mit Erzeugungsporfilen (Dreiecksverlegung)
        # Low Voltage
        "NAYY 4x50 SE":
        {"c_nf_per_km": 210,
            "r_ohm_per_km": 0.642,
            "x_ohm_per_km": 0.083,
            "max_i_ka": 0.142,
            "type": "cs",
            "q_mm2": 50,
            "alpha": alpha_al},
        "NAYY 4x120 SE":
        {"c_nf_per_km": 264,
            "r_ohm_per_km": 0.225,
            "x_ohm_per_km": 0.080,
            "max_i_ka": 0.242,
            "type": "cs",
            "q_mm2": 120,
            "alpha": alpha_al},
        "NAYY 4x150 SE":
        {"c_nf_per_km": 261,
            "r_ohm_per_km": 0.208,
            "x_ohm_per_km": 0.080,
            "max_i_ka": 0.270,
            "type": "cs",
            "q_mm2": 150,
            "alpha": alpha_al},
        # High Voltage
        "N2XS(FL)2Y 1x120 RM/35 64/110 kV":
        {"c_nf_per_km": 112,
            "r_ohm_per_km": 0.153,
            "x_ohm_per_km": 0.166,
            "max_i_ka": 0.366,
            "type": "cs",
            "q_mm2": 120,
            "alpha": alpha_cu},
        "N2XS(FL)2Y 1x185 RM/35 64/110 kV":
        {"c_nf_per_km": 125,
            "r_ohm_per_km": 0.099,
            "x_ohm_per_km": 0.156,
            "max_i_ka": 0.457,
            "type": "cs",
            "q_mm2": 185,
            "alpha": alpha_cu},
        "N2XS(FL)2Y 1x240 RM/35 64/110 kV":
        {"c_nf_per_km": 135,
            "r_ohm_per_km": 0.075,
            "x_ohm_per_km": 0.149,
            "max_i_ka": 0.526,
            "type": "cs",
            "q_mm2": 240,
            "alpha": alpha_cu},
        "N2XS(FL)2Y 1x300 RM/35 64/110 kV":
        {"c_nf_per_km": 144,
            "r_ohm_per_km": 0.060,
            "x_ohm_per_km": 0.144,
            "max_i_ka": 0.588,
            "type": "cs",
            "q_mm2": 300,
            "alpha": alpha_cu},

 */

private fun getAvailableLineStdTypes(id: String): WireInfo
{
    val list = mutableListOf<WireInfo>()
    //TODO: set up raetedCurrent with double values. Right now the attribute ratedCurrent is Int.
    list.add(OverheadWireInfo("N2XS(FL)2Y 1x300 RM/35 64/110 kV").apply {ratedCurrent = 0})
    list.add(OverheadWireInfo("NA2XS2Y 1x240 RM/25 12/20 kV").apply {ratedCurrent = 0})
    list.add(OverheadWireInfo("48-AL1/8-ST1A 20.0").apply {ratedCurrent = 0})
    return list.find {it.mRID == id}!!
}



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

