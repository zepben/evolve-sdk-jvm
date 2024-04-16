/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.BaseDatabaseWriter
import com.zepben.evolve.metrics.IngestionMetrics
import java.sql.Connection
import java.sql.DriverManager

class MetricsDatabaseWriter @JvmOverloads constructor(
    databaseFile: String,
    metrics: IngestionMetrics,
    databaseTables: MetricsDatabaseTables = MetricsDatabaseTables(),
    val createMetricsWriter: (Connection) -> MetricsWriter = { MetricsWriter(metrics, databaseTables) },
    getConnection: (String) -> Connection = DriverManager::getConnection
) : BaseDatabaseWriter(databaseFile, databaseTables, getConnection) {

    override fun saveWithConnection(connection: Connection): Boolean = createMetricsWriter(connection).save()

}
