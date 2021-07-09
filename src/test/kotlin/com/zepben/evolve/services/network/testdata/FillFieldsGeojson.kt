/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.testdata

import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.AuxiliaryEquipment
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.FaultIndicator
import com.zepben.evolve.cim.iec61970.base.core.ConnectivityNode
import com.zepben.evolve.cim.iec61970.base.core.Equipment
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.cim.iec61970.base.wires.Breaker
import com.zepben.evolve.cim.iec61970.base.wires.Fuse
import com.zepben.evolve.services.network.NetworkService


// Specialised fillFields functions to cater for specifics of GeoJSON.

/************ IEC61970 BASE AUXILIARY EQUIPMENT ************/

fun AuxiliaryEquipment.fillFieldsGeojson(service: NetworkService, includeRuntime: Boolean = true): AuxiliaryEquipment {
    (this as Equipment).fillFields(service, includeRuntime)

    val cn = ConnectivityNode()
    terminal = Terminal().also {
        cn.addTerminal(it)
        val ce1 = Fuse().apply {
            addTerminal(it)
            service.add(this)
        }
        it.conductingEquipment = ce1
        it.connectivityNode = cn

        service.add(it)
    }

    val ce2 = Breaker().apply {
        terminal = Terminal().also {
            it.conductingEquipment = this
            it.connectivityNode = cn
            cn.addTerminal(it)
            addTerminal(it)
            service.add(it)
        }
        service.add(this)
    }

    service.add(cn)

    return this
}

fun FaultIndicator.fillFieldsGeojson(service: NetworkService, includeRuntime: Boolean = true): FaultIndicator {
    (this as AuxiliaryEquipment).fillFieldsGeojson(service, includeRuntime)
    return this
}
