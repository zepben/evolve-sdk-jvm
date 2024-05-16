/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.TableVersion
import com.zepben.evolve.database.sqlite.extensions.configureBatch
import com.zepben.evolve.metrics.IngestionJob
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

class MetricsDataSourceWriter(
    private val dataSource: DataSource,
    private val databaseTables: MetricsDatabaseTables = MetricsDatabaseTables()
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val versionTable = databaseTables.getTable<TableVersion>()

    @Throws(IncompatibleVersionException::class)
    fun save(job: IngestionJob): Boolean {
        return dataSource.connection.use { connection ->
            connection.configureBatch()
            val localVersion = versionTable.supportedVersion
            when (val remoteVersion = getVersion(connection)) {
                null -> createSchema(connection) && populateTables(connection, job) && postSave(connection)
                localVersion -> populateTables(connection, job) && postSave(connection)
                else -> throw IncompatibleVersionException(localVersion, remoteVersion)
            }
        }
    }

    private fun createSchema(connection: Connection): Boolean =
        try {
            logger.info("No version table found. Creating database schema v${versionTable.supportedVersion}...")

            connection.createStatement().use { statement ->
                statement.queryTimeout = 2

                databaseTables.forEachTable { table ->
                    statement.executeUpdate(table.createTableSql)
                    table.createIndexesSql.forEach { sql ->
                        statement.executeUpdate(sql)
                    }
                }

                // Add the version number to the database.
                connection.prepareStatement(versionTable.preparedInsertSql).use { insert ->
                    insert.setInt(versionTable.VERSION.queryIndex, versionTable.supportedVersion)
                    insert.executeUpdate()
                }

                connection.commit()
                logger.info("Schema created.")
            }
            true
        } catch (e: SQLException) {
            logger.error("Failed to create database schema: " + e.message)
            false
        }

    private fun getVersion(connection: Connection): Int? =
        connection.createStatement().use { statement ->
            val tableVersion = databaseTables.getTable<TableVersion>()
            try {
                statement.executeQuery(tableVersion.selectSql).use { rs ->
                    if (rs.next()) {
                        rs.getInt(tableVersion.VERSION.queryIndex)
                    } else {
                        throw MissingVersionException
                    }
                }
            } catch (e: SQLException) {
                null
            }
        }

    internal fun populateTables(connection: Connection, job: IngestionJob): Boolean {
        databaseTables.prepareInsertStatements(connection)
        return MetricsWriter(job, databaseTables).save()
    }

    private fun postSave(connection: Connection): Boolean {
        connection.commit()
        return true
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

/**
 * Thrown if the version table in the remote metrics database has no entry.
 */
object MissingVersionException : Exception("Version table present in remote metrics database, but it missing an entry.")
