/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableTerminals : TableAcDcTerminals() {

    val CONDUCTING_EQUIPMENT_MRID: Column = Column(++columnIndex, "conducting_equipment_mrid", "TEXT", NULL)
    val SEQUENCE_NUMBER: Column = Column(++columnIndex, "sequence_number", "INTEGER", NOT_NULL)
    val CONNECTIVITY_NODE_MRID: Column = Column(++columnIndex, "connectivity_node_mrid", "TEXT", NULL)
    val PHASES: Column = Column(++columnIndex, "phases", "TEXT", NOT_NULL)

    override val name: String = "terminals"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(CONDUCTING_EQUIPMENT_MRID, SEQUENCE_NUMBER))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(CONNECTIVITY_NODE_MRID))
        }

}
