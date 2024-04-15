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
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

@Suppress("PropertyName")
class TableNameTypes : SqliteTable() {

    val NAME: Column = Column(++columnIndex, "name", "TEXT", NOT_NULL)
    val DESCRIPTION: Column = Column(++columnIndex, "description", "TEXT", NULL)

    override val name: String = "name_types"

    override val uniqueIndexColumns: MutableList<List<Column>> = mutableListOf(
        listOf(NAME)
    )

}
