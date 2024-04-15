/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.associations

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

@Suppress("PropertyName")
class TableUsagePointsEndDevices : SqliteTable() {

    val USAGE_POINT_MRID: Column = Column(++columnIndex, "usage_point_mrid", "TEXT", NOT_NULL)
    val END_DEVICE_MRID: Column = Column(++columnIndex, "end_device_mrid", "TEXT", NOT_NULL)

    override val name: String = "usage_points_end_devices"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(USAGE_POINT_MRID, END_DEVICE_MRID))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(USAGE_POINT_MRID))
            add(listOf(END_DEVICE_MRID))
        }

}
