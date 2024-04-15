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
class TableEquipmentEquipmentContainers : SqliteTable() {

    val EQUIPMENT_MRID: Column = Column(++columnIndex, "equipment_mrid", "TEXT", NOT_NULL)
    val EQUIPMENT_CONTAINER_MRID: Column = Column(++columnIndex, "equipment_container_mrid", "TEXT", NOT_NULL)

    override val name: String = "equipment_equipment_containers"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(EQUIPMENT_MRID, EQUIPMENT_CONTAINER_MRID))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(EQUIPMENT_MRID))
            add(listOf(EQUIPMENT_CONTAINER_MRID))
        }

}
