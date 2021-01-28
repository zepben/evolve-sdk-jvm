/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks

import com.zepben.evolve.cim.iec61970.base.core.BaseVoltage
import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.ConnectivityNode
import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.wires.EnergySource
import com.zepben.evolve.cim.iec61970.base.wires.Junction
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.network.NetworkService


/*
# create buses
    bus1 = pp.create_bus(net, name="HV Busbar", vn_kv=110., type="b")
    bus2 = pp.create_bus(net, name="HV Busbar 2", vn_kv=110., type="b")
    bus3 = pp.create_bus(net, name="HV Transformer Bus", vn_kv=110., type="n")
    bus4 = pp.create_bus(net, name="MV Transformer Bus", vn_kv=20., type="n")
    bus5 = pp.create_bus(net, name="MV Main Bus", vn_kv=20., type="b")
    bus6 = pp.create_bus(net, name="MV Bus 1", vn_kv=20., type="b")
    bus7 = pp.create_bus(net, name="MV Bus 2", vn_kv=20., type="b")
     # create external grid
    pp.create_ext_grid(net, bus1, vm_pu=1.02, va_degree=50)

    # create transformer
    pp.create_transformer(net, bus3, bus4, name="110kV/20kV transformer",
                                   std_type="25 MVA 110/20 kV")
    # create lines
    pp.create_line(net, bus1, bus2, length_km=10,
                           std_type="N2XS(FL)2Y 1x300 RM/35 64/110 kV", name="Line 1")
    line2 = pp.create_line(net, bus5, bus6, length_km=2.0,
                           std_type="NA2XS2Y 1x240 RM/25 12/20 kV", name="Line 2")
    line3 = pp.create_line(net, bus6, bus7, length_km=3.5,
                           std_type="48-AL1/8-ST1A 20.0", name="Line 3")
    line4 = pp.create_line(net, bus7, bus5, length_km=2.5,
                           std_type="NA2XS2Y 1x240 RM/25 12/20 kV", name="Line 4")

    # create bus-bus switches
    pp.create_switch(net, bus2, bus3, et="b", type="CB")
    pp.create_switch(net, bus4, bus5, et="b", type="CB")

    # create bus-line switches
    pp.create_switch(net, bus5, line2, et="l", type="LBS", closed=True)
    pp.create_switch(net, bus6, line2, et="l", type="LBS", closed=True)
    pp.create_switch(net, bus6, line3, et="l", type="LBS", closed=True)
    pp.create_switch(net, bus7, line3, et="l", type="LBS", closed=False)
    pp.create_switch(net, bus7, line4, et="l", type="LBS", closed=True)
    pp.create_switch(net, bus5, line4, et="l", type="LBS", closed=True)

    # create load
    pp.create_load(net, bus7, p_mw=2, q_mvar=4, scaling=0.6, name="load")

    # create generator
    pp.create_gen(net, bus6, p_mw=6, max_q_mvar=3, min_q_mvar=-3, vm_pu=1.03,
                  name="generator")

    # create static generator
    pp.create_sgen(net, bus7, p_mw=2, q_mvar=-0.5, name="static generator")

    # create shunt
    pp.create_shunt(net, bus3, q_mvar=-0.96, p_mw=0, name='Shunt')
 */

fun simpleNetwork(): BaseService {
    val net = NetworkService()
    val bv110 = BaseVoltage().apply { nominalVoltage = 110000}
    val bv20  = BaseVoltage().apply { nominalVoltage = 20000}
    val bus1 = net.createBus{name="HV Busbar";baseVoltage = bv110}
    val bus2 = net.createBus{ name="HV Busbar 2"; baseVoltage = bv110}
    net.createBus{name="HV Transformer Bus"; baseVoltage = bv110}
    net.createBus{name="MV Transformer Bus"; baseVoltage =bv20}
    net.createBus{name="MV Main Bus"; baseVoltage =bv20}
    net.createBus{name="MV Bus 1"; baseVoltage =bv20}
    net.createBus{name="MV Bus 2"; baseVoltage =bv20}
    net.createTransformer(2, info = "25 MVA 110/20 kV"){name = "110kV/20kV transformer"}
    // TODO: How to associated PowerTrandformerEndInfo to a PowerTranformerInfo?
    net.createEnergySource(){}
    net.createLine(bus1 = bus1, bus2 = bus2){name="Line 1"; length = 10.0}

    return net
}

fun main(){
    println(simpleNetwork().setOf<IdentifiedObject>())
}