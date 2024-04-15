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
class TableEquipmentOperationalRestrictions : SqliteTable() {

    val EQUIPMENT_MRID: Column = Column(++columnIndex, "equipment_mrid", "TEXT", NOT_NULL)
    val OPERATIONAL_RESTRICTION_MRID: Column =
        Column(++columnIndex, "operational_restriction_mrid", "TEXT", NOT_NULL)

    override val name: String = "equipment_operational_restrictions"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(EQUIPMENT_MRID, OPERATIONAL_RESTRICTION_MRID))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(EQUIPMENT_MRID))
            add(listOf(OPERATIONAL_RESTRICTION_MRID))
        }

}
