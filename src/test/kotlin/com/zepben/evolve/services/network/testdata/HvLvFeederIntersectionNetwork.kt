/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.testdata

import com.zepben.evolve.cim.iec61970.base.core.BaseVoltage
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.testing.TestNetworkBuilder

object HvLvFeederIntersectionNetwork {

    //
    // - or |: LV line
    // = or #: HV line
    //
    //          b2
    //          |
    //          | c3
    //      c1  |
    // b0 ======+
    //          |
    //          | c4
    //          |
    //
    // fdr5 head terminal is b0-t2
    // lvf6 head terminal is b2-t2
    //
    fun create(): NetworkService {
        val hvBaseVoltage = BaseVoltage().apply { nominalVoltage = 1000 }
        val lvBaseVoltage = BaseVoltage().apply { nominalVoltage = 999 }

        return TestNetworkBuilder()
            .fromBreaker { baseVoltage = hvBaseVoltage }
            .toAcls { baseVoltage = hvBaseVoltage }
            .fromBreaker { baseVoltage = lvBaseVoltage }
            .toAcls { baseVoltage = lvBaseVoltage }
            .toAcls { baseVoltage = lvBaseVoltage }
            .connect("c1", "c3", 2, 2)
            .addFeeder("b0")
            .addLvFeeder("b2", 2)
            .network
    }
}