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
class TableCircuitsSubstations : SqliteTable() {

    val CIRCUIT_MRID: Column = Column(++columnIndex, "circuit_mrid", "TEXT", NOT_NULL)
    val SUBSTATION_MRID: Column = Column(++columnIndex, "substation_mrid", "TEXT", NOT_NULL)

    override val name = "circuits_substations"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(CIRCUIT_MRID, SUBSTATION_MRID))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(CIRCUIT_MRID))
            add(listOf(SUBSTATION_MRID))
        }

}
