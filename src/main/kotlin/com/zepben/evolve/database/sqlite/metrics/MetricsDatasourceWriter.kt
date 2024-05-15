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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class MetricsDatasourceWriter(
    private val dataSource: DataSource,
    private val job: IngestionJob,
    private val databaseTables: MetricsDatabaseTables = MetricsDatabaseTables()
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun save() {
        val localVersion = databaseTables.getTable<TableVersion>().supportedVersion
        when (val remoteVersion = getVersion()) {
            null -> TODO("No version found, so create schema here (tables and indexes) and populate tables")
            localVersion -> TODO("Matching version found, so just populate tables")
            else -> throw IncompatibleVersionException(localVersion, remoteVersion)
        }
    }

    private fun getVersion(): Int? {
        TODO("Implement this. Should return null if no version table is found and the version number found otherwise.")
    }

}

/**
 * Indicates a difference between the local and remote version of the metrics database.
 *
 * @property localVersion The locally-supported version of the metrics database in the SDK.
 * @property remoteVersion The version of the remote metrics database.
 */
class IncompatibleVersionException(
    val localVersion: Int,
    val remoteVersion: Int
) : Exception("Incompatible version in remote metrics database: expected v$localVersion, found v$remoteVersion. " +
    "Please ${if (localVersion > remoteVersion) "upgrade the remote database" else "use a newer version of the SDK"}.")
