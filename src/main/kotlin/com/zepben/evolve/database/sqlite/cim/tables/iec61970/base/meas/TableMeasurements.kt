/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.meas

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableIdentifiedObjects

@Suppress("PropertyName")
abstract class TableMeasurements : TableIdentifiedObjects() {

    val POWER_SYSTEM_RESOURCE_MRID: Column = Column(++columnIndex, "power_system_resource_mrid", "TEXT", NULL)
    val REMOTE_SOURCE_MRID: Column = Column(++columnIndex, "remote_source_mrid", "TEXT", NULL)
    val TERMINAL_MRID: Column = Column(++columnIndex, "terminal_mrid", "TEXT", NULL)
    val PHASES: Column = Column(++columnIndex, "phases", "TEXT", NOT_NULL)
    val UNIT_SYMBOL: Column = Column(++columnIndex, "unit_symbol", "TEXT", NOT_NULL)

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(POWER_SYSTEM_RESOURCE_MRID))
            add(listOf(REMOTE_SOURCE_MRID))
            add(listOf(TERMINAL_MRID))
        }

}
