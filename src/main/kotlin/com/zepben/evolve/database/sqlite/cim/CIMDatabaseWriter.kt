/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim

import com.zepben.evolve.database.sqlite.cim.metadata.MetadataCollectionWriter
import com.zepben.evolve.database.sqlite.common.BaseDatabaseWriter
import com.zepben.evolve.database.sqlite.common.TableVersion
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.SQLException

/**
 * A base class for writing objects to one of our CIM databases.
 *
 * @param databaseFile The filename of the database to write.
 * @param databaseTables The tables to create in the database.
 * @param getConnection Provider of the connection to the specified database.
 * @param metadataWriter The [MetadataCollectionWriter] to use.
 * @param serviceWriter The [BaseServiceWriter] to use.
 *
 * @property logger The logger to use for this database writer.
 */
abstract class CimDatabaseWriter(
    private val databaseFile: String,
    databaseTables: CimDatabaseTables,
    getConnection: (String) -> Connection,
    private val metadataWriter: MetadataCollectionWriter,
    private val serviceWriter: BaseServiceWriter
) : BaseDatabaseWriter(databaseTables, { getConnection("jdbc:sqlite:$databaseFile") }) {

    /**
     * Save metadata and service.
     */
    override fun saveSchema(): Boolean = metadataWriter.save() and serviceWriter.save()

    override fun preConnect(): Boolean =
        try {
            Files.deleteIfExists(Paths.get(databaseFile))
            true
        } catch (e: IOException) {
            logger.error("Unable to save database, failed to remove previous instance: " + e.message)
            false
        }

    override fun preSave(): Boolean =
        try {
            val versionTable = databaseTables.getTable<TableVersion>()
            logger.info("Creating database schema v${versionTable.supportedVersion}...")

            saveConnection.createStatement().use { statement ->
                statement.queryTimeout = 2

                databaseTables.forEachTable {
                    statement.executeUpdate(it.createTableSql)
                }

                // Add the version number to the database.
                saveConnection.prepareStatement(versionTable.preparedInsertSql).use { insert ->
                    insert.setInt(versionTable.VERSION.queryIndex, versionTable.supportedVersion)
                    insert.executeUpdate()
                }

                saveConnection.commit()
                logger.info("Schema created.")
            }
            true
        } catch (e: SQLException) {
            logger.error("Failed to create database schema: " + e.message)
            false
        }

    override fun postSave(): Boolean =
        try {
            logger.info("Adding indexes...")

            saveConnection.createStatement().use { statement ->
                databaseTables.forEachTable { table ->
                    table.createIndexesSql.forEach { sql ->
                        statement.execute(sql)
                    }
                }
            }

            logger.info("Indexes added.")
            logger.info("Committing...")

            saveConnection.commit()

            logger.info("Done.")
            true
        } catch (e: SQLException) {
            logger.error("Failed to finalise the database: " + e.message)
            false
        }

}
