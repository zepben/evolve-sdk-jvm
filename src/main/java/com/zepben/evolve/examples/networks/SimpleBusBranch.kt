/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.examples.networks

import com.zepben.evolve.cim.iec61970.base.core.BaseVoltage
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.network.NetworkService


fun simpleBusBranchNetwork(): BaseService {

    // Create empty network
    val net = NetworkService()

    // Create buses
    val bvHV = BaseVoltage().apply { nominalVoltage = 20000}
    val bvLV = BaseVoltage().apply { nominalVoltage = 400}
    val b1 = net.createBus(bvHV){name = "Bus 1"}
    val b2 = net.createBus(bvLV){name= "Bus 2"}
    val b3 = net.createBus(bvLV){name= "Bus 3"}

    // Create bus elements
    net.createEnergySource(bus = b1){voltageMagnitude = 1.02*bvHV.nominalVoltage; name = "Grid Connection"}
    net.createLoad(bus = b3){p = 100000.0; q =50000.0; name = "Load"}

    // Create branch elements
    val ptinfo = net.getAvailablePowerTransformerInfo("0.4 MVA 20/0.4 kV")?: error("Not TransformerInfo found")
    net.createTransformer(bus1 = b1, bus2 = b2, ptInfo = ptinfo){name="Trafo"}
    net.createLine(bus1 = b2, bus2 = b3){length = 100.0; name="Line"}
    return net
}
