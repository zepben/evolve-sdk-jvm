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
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

@Suppress("PropertyName")
class TableNetworkContainerMetrics : SqliteTable() {

    val JOB_ID: Column = Column(++columnIndex, "job_id", "TEXT", NOT_NULL)
    val HIERARCHY_ID: Column = Column(++columnIndex, "hierarchy_id", "TEXT", NULL)
    val HIERARCHY_NAME: Column = Column(++columnIndex, "hierarchy_name", "TEXT", NULL)
    val CONTAINER_TYPE: Column = Column(++columnIndex, "container_type", "TEXT", NOT_NULL)
    val DIST_TX_COUNT: Column = Column(++columnIndex, "dist_tx_count", "NUMBER", NULL) // Example metric, list to be finalized

    override val uniqueIndexColumns: MutableList<List<Column>> = mutableListOf(
        listOf(JOB_ID, HIERARCHY_ID)
    )

    override val nonUniqueIndexColumns: MutableList<List<Column>> = mutableListOf(
        listOf(HIERARCHY_ID)
    )

    override val name: String = "network_container_metrics"

}
