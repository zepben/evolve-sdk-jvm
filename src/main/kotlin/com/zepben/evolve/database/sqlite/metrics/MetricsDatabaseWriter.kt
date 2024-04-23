/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.BaseDatabaseWriter
import com.zepben.evolve.metrics.IngestionJob
import java.sql.Connection
import java.sql.DriverManager

/**
 * Class for writing an ingestion job (and associated metadata, metrics, and sources) to a metrics database.
 *
 * @param databaseFile The filename of the metrics database.
 * @param job The ingestion job to write.
 * @param databaseTables The tables in the database.
 * @param createMetricsWriter The function to use to create the metrics writer from a connection.
 * @param getConnection Provider of the connection to the specified database.
 */
class MetricsDatabaseWriter @JvmOverloads constructor(
    databaseFile: String,
    job: IngestionJob,
    databaseTables: MetricsDatabaseTables = MetricsDatabaseTables(),
    val createMetricsWriter: (Connection) -> MetricsWriter = { MetricsWriter(job, databaseTables) },
    getConnection: (String) -> Connection = DriverManager::getConnection
) : BaseDatabaseWriter(databaseFile, databaseTables, getConnection) {

    /**
     * Save the ingestion job (and associated data) with the specified connection.
     *
     * @param connection The connection to use for saving the job.
     */
    override fun saveWithConnection(connection: Connection): Boolean = createMetricsWriter(connection).save()

}
