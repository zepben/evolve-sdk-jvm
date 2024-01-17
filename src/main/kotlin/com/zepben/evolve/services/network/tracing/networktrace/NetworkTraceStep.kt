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
class NetworkTraceStep<T>(
    val path: StepPath,
    val data: T?
)

sealed interface StepPath {
    val fromTerminal: Terminal?
    val fromEquipment: ConductingEquipment
    val toTerminal: Terminal?
    val toEquipment: ConductingEquipment
    val numTerminalSteps: Int
    val numEquipmentSteps: Int

    val tracedInternally: Boolean
        get() = fromTerminal != null && toTerminal != null && fromEquipment == toEquipment
}

class TerminalToTerminalPath(
    override val fromTerminal: Terminal,
    override val toTerminal: Terminal,
    override val numTerminalSteps: Int,
    override val numEquipmentSteps: Int,
) : StepPath {
    override val fromEquipment: ConductingEquipment
        get() = fromTerminal.conductingEquipment ?: error("Network trace does not support terminals that do not have conducting equipment")
    override val toEquipment: ConductingEquipment
        get() = toTerminal.conductingEquipment ?: error("Network trace does not support terminals that do not have conducting equipment")
}

/**
 * Here for future clamp support. Clamps will always connect equipment to equipment without terminals with AcLineSegment.
 * It probably needs a better name before it is introduced though!
 */
//class DirectEquipmentPath(
//    override val fromEquipment: ConductingEquipment,
//    override val toEquipment: ConductingEquipment,
//    override val nTerminalSteps: Int,
//    override val nEquipmentSteps: Int,
//) : StepPath {
//    override val fromTerminal: Terminal? get() = null
//    override val toTerminal: Terminal? get() = null
//}
