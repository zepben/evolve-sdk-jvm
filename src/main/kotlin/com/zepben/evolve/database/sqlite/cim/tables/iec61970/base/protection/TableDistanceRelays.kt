/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.protection

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableDistanceRelays : TableProtectionRelayFunctions() {

    val BACKWARD_BLIND: Column = Column(++columnIndex, "backward_blind", "NUMBER", NULL)
    val BACKWARD_REACH: Column = Column(++columnIndex, "backward_reach", "NUMBER", NULL)
    val BACKWARD_REACTANCE: Column = Column(++columnIndex, "backward_reactance", "NUMBER", NULL)
    val FORWARD_BLIND: Column = Column(++columnIndex, "forward_blind", "NUMBER", NULL)
    val FORWARD_REACH: Column = Column(++columnIndex, "forward_reach", "NUMBER", NULL)
    val FORWARD_REACTANCE: Column = Column(++columnIndex, "forward_reactance", "NUMBER", NULL)
    val OPERATION_PHASE_ANGLE1: Column = Column(++columnIndex, "operation_phase_angle1", "NUMBER", NULL)
    val OPERATION_PHASE_ANGLE2: Column = Column(++columnIndex, "operation_phase_angle2", "NUMBER", NULL)
    val OPERATION_PHASE_ANGLE3: Column = Column(++columnIndex, "operation_phase_angle3", "NUMBER", NULL)

    override val name: String = "distance_relays"

}
