/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.tables.metrics

import com.zepben.evolve.database.sqlite.tables.Column
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.tables.SqliteTable

@Suppress("PropertyName")
class TableJobs : SqliteTable() {

    val JOB_ID: Column = Column(++columnIndex, "job_id", "TEXT", NOT_NULL)
    val INGEST_TIME: Column = Column(++columnIndex, "ingest_time", "TEXT", NOT_NULL)
    val SOURCE: Column = Column(++columnIndex, "source", "TEXT", NOT_NULL)
    val APPLICATION: Column = Column(++columnIndex, "application", "TEXT", NOT_NULL)
    val APPLICATION_VERSION: Column = Column(++columnIndex, "application_version", "TEXT", NOT_NULL)

    override val uniqueIndexColumns: MutableList<List<Column>> = mutableListOf(
        listOf(JOB_ID)
    )

    override val nonUniqueIndexColumns: MutableList<List<Column>> = mutableListOf(
        listOf(INGEST_TIME),
        listOf(SOURCE),
        listOf(APPLICATION, APPLICATION_VERSION)
    )

    override val name: String = "jobs"

}
