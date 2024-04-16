/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.BaseEntryWriter
import com.zepben.evolve.database.sqlite.extensions.setInstant
import com.zepben.evolve.database.sqlite.metrics.tables.TableJobSources
import com.zepben.evolve.database.sqlite.metrics.tables.TableJobs
import com.zepben.evolve.database.sqlite.metrics.tables.TableNetworkContainerMetrics
import com.zepben.evolve.metrics.*
import java.util.*

class MetricsEntryWriter(
    private val databaseTables: MetricsDatabaseTables,
    private val jobId: UUID
) : BaseEntryWriter() {

    fun save(metadata: IngestionMetadata): Boolean {
        val table = databaseTables.getTable<TableJobs>()
        val insert = databaseTables.getInsert<TableJobs>()

        insert.setString(table.JOB_ID.queryIndex, jobId.toString())
        insert.setInstant(table.INGEST_TIME.queryIndex, metadata.startTime)
        insert.setString(table.SOURCE.queryIndex, metadata.source)
        insert.setString(table.APPLICATION.queryIndex, metadata.application)
        insert.setString(table.APPLICATION_VERSION.queryIndex, metadata.applicationVersion)

        return insert.tryExecuteSingleUpdate("job")
    }

    fun saveSource(jobSource: JobSource): Boolean {
        val table = databaseTables.getTable<TableJobSources>()
        val insert = databaseTables.getInsert<TableJobSources>()
        val (sourceName, sourceMetadata) = jobSource

        insert.setString(table.JOB_ID.queryIndex, jobId.toString())
        insert.setString(table.DATA_SOURCE.queryIndex, sourceName)
        insert.setObject(table.FILE_SHA.queryIndex, sourceMetadata.fileHash) // TODO test if this works for sqlite

        return insert.tryExecuteSingleUpdate("job source")
    }

    fun saveMetric(networkMetric: NetworkMetric): Boolean {
        val table = databaseTables.getTable<TableNetworkContainerMetrics>()
        val insert = databaseTables.getInsert<TableNetworkContainerMetrics>()
        val (container, containerMetric) = networkMetric

        insert.setString(table.JOB_ID.queryIndex, jobId.toString())
        when (container) {
            is TotalNetworkContainer -> {
                insert.setString(table.CONTAINER_TYPE.queryIndex, "TOTAL")
            }
            is PartialNetworkContainer -> {
                insert.setString(table.HIERARCHY_ID.queryIndex, container.mRID)
                insert.setString(table.HIERARCHY_NAME.queryIndex, container.name)
                insert.setString(table.CONTAINER_TYPE.queryIndex, container.level.name)
            }
        }

        val metricEntries = listOf(
            "NUM_DIST_TX" to containerMetric.numDistTx
        )
        metricEntries.forEach { (metricName, metricValue) ->
            insert.setString(table.METRIC_NAME.queryIndex, metricName)
            insert.setObject(table.METRIC_VALUE.queryIndex, metricValue)
            insert.addBatch()
        }

        return insert.executeBatch().none { it < 0 }
    }

}
