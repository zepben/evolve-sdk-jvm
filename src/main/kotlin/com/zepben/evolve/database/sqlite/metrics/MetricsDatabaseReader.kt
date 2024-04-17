/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.extensions.getInstant
import com.zepben.evolve.database.sqlite.metrics.tables.TableJobs
import com.zepben.evolve.database.sqlite.metrics.tables.prepareSelectJobStatement
import com.zepben.evolve.database.sqlite.metrics.tables.tableMetricsVersion
import com.zepben.evolve.metrics.IngestionMetadata
import com.zepben.evolve.metrics.IngestionMetrics
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.*

class MetricsDatabaseReader(
    private val connection: Connection,
    private val tables: MetricsDatabaseTables = MetricsDatabaseTables()
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val supportedVersion = tableMetricsVersion.supportedVersion

    fun load(jobId: UUID): IngestionMetrics? {
        connection.createStatement().use { statement ->
            val version = tableMetricsVersion.getVersion(statement)
            if (version == supportedVersion) {
                logger.info("Loading from metrics database version v$version")
            } else {
                logger.error(formatVersionError(version))
                return null
            }
        }

        val jobsTable = tables.getTable<TableJobs>()
        val ingestionMetadata = connection.prepareSelectJobStatement(jobsTable, jobId).use { statement ->
            statement.setString(1, jobId.toString())
            statement.executeQuery().use { rs ->
                if (rs.next())
                    IngestionMetadata(
                        startTime = rs.getInstant(jobsTable.INGEST_TIME.queryIndex) ?: return null,
                        source = rs.getString(jobsTable.SOURCE.queryIndex),
                        application = rs.getString(jobsTable.APPLICATION.queryIndex),
                        applicationVersion = rs.getString(jobsTable.APPLICATION_VERSION.queryIndex)
                    )
                else return null
            }
        }
        val metrics = IngestionMetrics(jobId, ingestionMetadata)
        return metrics.takeIf { MetricsReader(metrics, tables, connection).load() }
    }

    private fun formatVersionError(version: Int?): String =
        when {
            version == null -> "Failed to read the version number from the selected database. Are you sure it is a EWB database?"
            version < supportedVersion -> unexpectedVersion(version, "Please upgrade the database to the newer version.")
            else -> unexpectedVersion(version, "You need to use a newer version of the SDK to load this database.")
        }

    private fun unexpectedVersion(version: Int?, action: String) =
        "Unable to load from metrics database [found v$version, expected v$supportedVersion]. $action"

}
