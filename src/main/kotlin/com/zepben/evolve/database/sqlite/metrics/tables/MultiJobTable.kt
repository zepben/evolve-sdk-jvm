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
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

/**
 * Table that has ingestion job ID as its primary key or part of its composite key.
 */
@Suppress("PropertyName")
abstract class MultiJobTable : SqliteTable() {

    val JOB_ID: Column = Column(++columnIndex, "job_id", "TEXT", NOT_NULL)

}
