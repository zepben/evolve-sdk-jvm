/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableConductingEquipment

@Suppress("PropertyName")
abstract class TableConductors : TableConductingEquipment() {

    val LENGTH: Column = Column(++columnIndex, "length", "NUMBER", NULL)
    val WIRE_INFO_MRID: Column = Column(++columnIndex, "wire_info_mrid", "TEXT", NULL)

}
