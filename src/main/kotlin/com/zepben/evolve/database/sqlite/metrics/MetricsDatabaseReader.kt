/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.TableVersion
import com.zepben.evolve.metrics.IngestionJob
import com.zepben.evolve.metrics.IngestionJobCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.*

class MetricsDatabaseReader(
    private val connection: Connection,
    private val metricsCollection: IngestionJobCollection,
    private val tables: MetricsDatabaseTables = MetricsDatabaseTables(),
    private val reader: MetricsReader = MetricsReader(, tables, connection)
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val tableVersion: TableVersion = tables.getTable()
    private val supportedVersion = tableVersion.supportedVersion

    fun load(): Boolean = checkVersion() && reader.load()

    fun load(jobId: UUID): IngestionJob? {
        return if (checkVersion() && reader.load(jobId))
            metricsCollection[jobId]
        else
            null
    }

    private fun checkVersion(): Boolean {
        val version = tableVersion.getVersion(connection)
        return if (version == supportedVersion) {
            logger.info("Loading from metrics database version v$version")
            true
        } else {
            logger.error(formatVersionError(version))
            false
        }
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
