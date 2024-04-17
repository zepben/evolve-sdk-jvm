/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.BaseCollectionReader
import com.zepben.evolve.database.sqlite.extensions.getNullableDouble
import com.zepben.evolve.database.sqlite.metrics.tables.MultiJobTable
import com.zepben.evolve.database.sqlite.metrics.tables.TableJobSources
import com.zepben.evolve.database.sqlite.metrics.tables.TableNetworkContainerMetrics
import com.zepben.evolve.database.sqlite.metrics.tables.prepareSelectJobStatement
import com.zepben.evolve.metrics.IngestionMetrics
import com.zepben.evolve.metrics.NetworkLevel
import com.zepben.evolve.metrics.PartialNetworkContainer
import com.zepben.evolve.metrics.TotalNetworkContainer
import java.sql.Connection
import java.sql.ResultSet

class MetricsReader(
    private val metrics: IngestionMetrics,
    databaseTables: MetricsDatabaseTables,
    connection: Connection
) : BaseCollectionReader(databaseTables, connection) {

    override fun load(): Boolean =
        loadEach<TableJobSources>(::load, ::prepareStatement)
            .andLoadEach<TableNetworkContainerMetrics>(::load, ::prepareStatement)

    private fun load(table: TableJobSources, rs: ResultSet, setIdentifier: (String) -> String): Boolean {
        val metadata = metrics.jobSources[setIdentifier(rs.getString(table.DATA_SOURCE.queryIndex))]
        metadata.fileHash = rs.getClob(table.FILE_SHA.queryIndex).takeUnless { rs.wasNull() }?.asciiStream?.readAllBytes()
        return true
    }

    private fun load(table: TableNetworkContainerMetrics, rs: ResultSet, setIdentifier: (String) -> String): Boolean {
        val networkContainer = when (val levelString = rs.getString(table.CONTAINER_TYPE.queryIndex)) {
            "TOTAL" -> TotalNetworkContainer
            else -> PartialNetworkContainer(
                level = NetworkLevel.valueOf(levelString),
                mRID = rs.getString(table.HIERARCHY_ID.queryIndex),
                name = rs.getString(table.HIERARCHY_NAME.queryIndex)
            )
        }
        val metricName = rs.getString(table.METRIC_NAME.queryIndex)
        setIdentifier("$networkContainer: $metricName")

        metrics.networkMetrics[networkContainer][rs.getString(table.METRIC_NAME.queryIndex)] = rs.getNullableDouble(table.METRIC_VALUE.queryIndex)
        return true
    }

    private fun prepareStatement(connection: Connection, table: MultiJobTable) = connection.prepareSelectJobStatement(table, metrics.jobId)

}
