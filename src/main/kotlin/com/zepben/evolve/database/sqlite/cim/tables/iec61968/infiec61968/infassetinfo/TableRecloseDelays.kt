/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.infiec61968.infassetinfo

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

@Suppress("PropertyName")
class TableRecloseDelays : SqliteTable() {

    val RELAY_INFO_MRID: Column = Column(++columnIndex, "relay_info_mrid", "TEXT", NOT_NULL)
    val RECLOSE_DELAY: Column = Column(++columnIndex, "reclose_delay", "NUMBER", NOT_NULL)
    val SEQUENCE_NUMBER: Column = Column(++columnIndex, "sequence_number", "INTEGER", NOT_NULL)

    override val name: String = "reclose_delays"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(RELAY_INFO_MRID, SEQUENCE_NUMBER))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(RELAY_INFO_MRID))
        }

    override val selectSql: String =
        "${super.selectSql} ORDER BY relay_info_mrid, sequence_number ASC;"

}
