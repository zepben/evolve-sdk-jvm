/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.BaseCollectionReader
import com.zepben.evolve.database.sqlite.extensions.getInstant
import com.zepben.evolve.database.sqlite.extensions.getNullableDouble
import com.zepben.evolve.database.sqlite.metrics.tables.*
import com.zepben.evolve.metrics.*
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

class MetricsReader(
    private val metricsCollection: IngestionMetricsCollection,
    databaseTables: MetricsDatabaseTables,
    connection: Connection
) : BaseCollectionReader(databaseTables, connection) {

    override fun load(): Boolean =
        loadEach<TableJobs>(::load)
            .andLoadEach<TableJobSources>(::load)
            .andLoadEach<TableNetworkContainerMetrics>(::load)

    fun load(jobId: UUID): Boolean =
        loadEach<TableJobs>(::load) { prepareSelectJobStatement(it, jobId) }
            .andLoadEach<TableJobSources>(::load) { prepareSelectJobStatement(it, jobId) }
            .andLoadEach<TableNetworkContainerMetrics>(::load) { prepareSelectJobStatement(it, jobId) }

    private fun load(table: TableJobs, rs: ResultSet, setIdentifier: (String) -> String): Boolean {
        metricsCollection[setIdentifier(rs.getString(table.JOB_ID.queryIndex))].metadata =
            IngestionMetadata(
                startTime = rs.getInstant(table.INGEST_TIME.queryIndex)!!,
                source = rs.getString(table.SOURCE.queryIndex),
                application = rs.getString(table.APPLICATION.queryIndex),
                applicationVersion = rs.getString(table.APPLICATION_VERSION.queryIndex)
            )
        return true
    }

    private fun load(table: TableJobSources, rs: ResultSet, setIdentifier: (String) -> String): Boolean {
        val jobId = rs.getString(table.JOB_ID.queryIndex)
        val dataSourceName = rs.getString(table.DATA_SOURCE.queryIndex)
        setIdentifier("$jobId-source-$dataSourceName")

        metricsCollection[jobId].jobSources[dataSourceName].fileHash =
            rs.getClob(table.FILE_SHA.queryIndex).takeUnless { rs.wasNull() }?.asciiStream?.readAllBytes()

        return true
    }

    private fun load(table: TableNetworkContainerMetrics, rs: ResultSet, setIdentifier: (String) -> String): Boolean {
        val jobId = rs.getString(table.JOB_ID.queryIndex)
        val networkContainer = when (val levelString = rs.getString(table.CONTAINER_TYPE.queryIndex)) {
            "TOTAL" -> TotalNetworkContainer
            else -> PartialNetworkContainer(
                level = NetworkLevel.valueOf(levelString),
                mRID = rs.getString(table.HIERARCHY_ID.queryIndex),
                name = rs.getString(table.HIERARCHY_NAME.queryIndex)
            )
        }
        val metricName = rs.getString(table.METRIC_NAME.queryIndex)
        setIdentifier("$jobId-container-$networkContainer-metric-$metricName")

        metricsCollection[jobId].networkMetrics[networkContainer][rs.getString(table.METRIC_NAME.queryIndex)] =
            rs.getNullableDouble(table.METRIC_VALUE.queryIndex)

        return true
    }

}
