/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.protection

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

@Suppress("PropertyName")
class TableProtectionRelayFunctionThresholds : SqliteTable() {

    val PROTECTION_RELAY_FUNCTION_MRID: Column = Column(++columnIndex, "protection_relay_function_mrid", "TEXT", NOT_NULL)
    val SEQUENCE_NUMBER: Column = Column(++columnIndex, "sequence_number", "INTEGER", NOT_NULL)
    val UNIT_SYMBOL: Column = Column(++columnIndex, "unit_symbol", "TEXT", NOT_NULL)
    val VALUE: Column = Column(++columnIndex, "value", "NUMBER", NOT_NULL)
    val NAME: Column = Column(++columnIndex, "name", "TEXT", NULL)

    override val name: String = "protection_relay_function_thresholds"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(PROTECTION_RELAY_FUNCTION_MRID, SEQUENCE_NUMBER))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(PROTECTION_RELAY_FUNCTION_MRID))
        }

}
