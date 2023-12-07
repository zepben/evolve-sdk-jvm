/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace

import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.Terminal

class NetworkTraceStep<T>(
    val fromTerminal: Terminal,
    val toTerminal: Terminal,
    val nTerminalSteps: Int,
    val nEquipmentSteps: Int,
    val data: T?
) {
    val fromEquipment: ConductingEquipment
        get() = requireNotNull(fromTerminal.conductingEquipment) {
            "Network trace does not support terminals that do not have conducting equipment"
        }

    val toEquipment: ConductingEquipment
        get() = requireNotNull(toTerminal.conductingEquipment) {
            "Network trace does not support terminals that do not have conducting equipment"
        }

    val steppedInternally get() = fromTerminal.conductingEquipment == toTerminal.conductingEquipment
}