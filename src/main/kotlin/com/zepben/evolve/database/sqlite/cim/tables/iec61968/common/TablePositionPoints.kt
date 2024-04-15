/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.common

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

@Suppress("PropertyName")
class TablePositionPoints : SqliteTable() {

    val LOCATION_MRID: Column = Column(++columnIndex, "location_mrid", "TEXT", NOT_NULL)
    val SEQUENCE_NUMBER: Column = Column(++columnIndex, "sequence_number", "INTEGER", NOT_NULL)
    val X_POSITION: Column = Column(++columnIndex, "x_position", "NUMBER", NOT_NULL)
    val Y_POSITION: Column = Column(++columnIndex, "y_position", "NUMBER", NOT_NULL)

    override val name: String = "position_points"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(LOCATION_MRID, SEQUENCE_NUMBER))
        }

}
