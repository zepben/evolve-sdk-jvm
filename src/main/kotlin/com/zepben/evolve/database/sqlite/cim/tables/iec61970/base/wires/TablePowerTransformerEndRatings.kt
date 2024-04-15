/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

@Suppress("PropertyName")
class TablePowerTransformerEndRatings : SqliteTable() {

    val POWER_TRANSFORMER_END_MRID: Column = Column(++columnIndex, "power_transformer_end_mrid", "TEXT", NOT_NULL)
    val COOLING_TYPE: Column = Column(++columnIndex, "cooling_type", "TEXT", NOT_NULL)
    val RATED_S: Column = Column(++columnIndex, "rated_s", "INTEGER", NOT_NULL)

    override val name: String = "power_transformer_end_ratings"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(POWER_TRANSFORMER_END_MRID, COOLING_TYPE))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(POWER_TRANSFORMER_END_MRID))
        }

}
