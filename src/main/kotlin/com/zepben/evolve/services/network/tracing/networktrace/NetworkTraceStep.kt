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

// TODO: Decent doco about why this is private and why we have other constructors. That is future proofing for Clamps.
sealed interface NetworkTraceStep<T> {
    val fromTerminal: Terminal?
    val fromEquipment: ConductingEquipment
    val toTerminal: Terminal?
    val toEquipment: ConductingEquipment
    val nTerminalSteps: Int
    val nEquipmentSteps: Int
    val data: T?
    val steppedInternally: Boolean
        get() = fromTerminal != null && toTerminal != null && fromEquipment == toEquipment
}

class TerminalToTerminalTraceStep<T>(
    override val fromTerminal: Terminal,
    override val toTerminal: Terminal,
    override val nTerminalSteps: Int,
    override val nEquipmentSteps: Int,
    override val data: T?
) : NetworkTraceStep<T> {
    override val fromEquipment: ConductingEquipment =
        fromTerminal.conductingEquipment ?: error("Network trace does not support terminals that do not have conducting equipment")
    override val toEquipment: ConductingEquipment =
        toTerminal.conductingEquipment ?: error("Network trace does not support terminals that do not have conducting equipment")
}

/**
 * Here for future clamp support. Clamps will always connect equipment to equipment without terminals with AcLineSegment.
 * It needs a better name before it is introduced though!
 */
//class NoTerminalTraceStep<T>(
//    override val fromEquipment: ConductingEquipment,
//    override val toEquipment: ConductingEquipment,
//    override val nTerminalSteps: Int,
//    override val nEquipmentSteps: Int,
//    override val data: T?
//) : NetworkTraceStep<T> {
//    override val fromTerminal: Terminal? = null
//    override val toTerminal: Terminal? = null
//}
