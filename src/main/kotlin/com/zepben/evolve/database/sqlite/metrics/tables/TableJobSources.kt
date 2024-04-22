/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics.tables

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableJobSources : MultiJobTable() {

    val DATA_SOURCE: Column = Column(++columnIndex, "data_source", "TEXT", NOT_NULL)
    val SOURCE_TIME: Column = Column(++columnIndex, "source_time", "TEXT", NULL)
    val FILE_SHA: Column = Column(++columnIndex, "file_sha", "BLOB", NULL)

    override val uniqueIndexColumns: MutableList<List<Column>> = mutableListOf(
        listOf(JOB_ID, DATA_SOURCE)
    )

    override val nonUniqueIndexColumns: MutableList<List<Column>> = mutableListOf(
        listOf(FILE_SHA)
    )

    override val name: String = "job_sources"

}
