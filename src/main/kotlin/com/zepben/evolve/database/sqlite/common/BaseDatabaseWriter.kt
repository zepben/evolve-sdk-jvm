/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common

import com.zepben.evolve.database.sqlite.cim.BaseServiceWriter
import com.zepben.evolve.database.sqlite.cim.metadata.MetadataCollectionWriter
import com.zepben.evolve.database.sqlite.cim.tables.MissingTableConfigException
import com.zepben.evolve.database.sqlite.extensions.configureBatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.SQLException

/**
 * A base class for writing objects to one of our databases.
 *
 * @param databaseFile The filename of the database to write.
 * @param databaseTables The tables to create in the database.
 * @param getConnection Provider of the connection to the specified database.
 *
 * @property logger The logger to use for this database writer.
 */
abstract class BaseDatabaseWriter(
    private val databaseFile: String,
    private val databaseTables: BaseDatabaseTables,
    private val getConnection: (String) -> Connection,
    private val persistFile: Boolean = false
) {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val databaseDescriptor: String = "jdbc:sqlite:$databaseFile"
    private lateinit var saveConnection: Connection
    private var hasBeenUsed: Boolean = false
    private var writingToExistingFile = false

    /**
     * Save the database using the [MetadataCollectionWriter] and [BaseServiceWriter].
     *
     * @return true if the database was successfully saved, otherwise false.
     */
    fun save(): Boolean {
        if (hasBeenUsed) {
            logger.error("You can only use the database writer once.")
            return false
        }
        hasBeenUsed = true

        if (!preSave()) {
            closeConnection()
            return false
        }

        val status = try {
            populateTables()
        } catch (e: MissingTableConfigException) {
            logger.error("Unable to save database: " + e.message, e)
            false
        }

        return status and postSave()
    }

    /**
     * Populate tables with entries determined by the model for this database writer.
     *
     * @return true if the model was saved successfully.
     */
    internal abstract fun populateTables(): Boolean

    private fun preSave(): Boolean =
        if (persistFile && Files.exists(Paths.get(databaseFile))) {
            writingToExistingFile = true
            logger.info("Connecting to existing database $databaseFile...")
            connect() && versionMatches() && prepareInsertStatements()
        } else {
            removeExisting()
                && connect()
                && create()
                && prepareInsertStatements()
        }

    private fun removeExisting(): Boolean =
        try {
            Files.deleteIfExists(Paths.get(databaseFile))
            true
        } catch (e: IOException) {
            logger.error("Unable to save database, failed to remove previous instance: " + e.message)
            false
        }

    private fun connect(): Boolean =
        try {
            saveConnection = getConnection(databaseDescriptor).configureBatch()
            true
        } catch (e: SQLException) {
            logger.error("Failed to connect to the database for saving: " + e.message)
            closeConnection()
            false
        }

    private fun prepareInsertStatements(): Boolean =
        try {
            databaseTables.prepareInsertStatements(saveConnection)
            true
        } catch (e: SQLException) {
            logger.error("Failed to prepare insert statements: " + e.message, e)
            closeConnection()
            false
        }

    private fun create(): Boolean =
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

    private fun versionMatches(): Boolean {
        saveConnection.createStatement().use { statement ->
            val tableVersion = databaseTables.getTable<TableVersion>()
            statement.executeQuery(tableVersion.selectSql).use { rs ->
                if (rs.next()) {
                    val version = rs.getInt(tableVersion.VERSION.queryIndex)
                    return if (version == tableVersion.supportedVersion) true
                    else {
                        logger.error("Unsupported version in database file (got $version, expected ${tableVersion.supportedVersion}).")
                        false
                    }
                } else {
                    logger.error("Missing version table in database file, cannot check compatibility")
                    return false
                }
            }
        }
    }

    private fun closeConnection() {
        try {
            if (::saveConnection.isInitialized)
                saveConnection.close()
        } catch (e: SQLException) {
            logger.error("Failed to close connection to database: " + e.message)
        }
    }

    private fun postSave(): Boolean =
        try {
            if (!writingToExistingFile) {
                logger.info("Adding indexes...")

                saveConnection.createStatement().use { statement ->
                    databaseTables.forEachTable { table ->
                        table.createIndexesSql.forEach { sql ->
                            statement.execute(sql)
                        }
                    }
                }

                logger.info("Indexes added.")
            }
            logger.info("Committing...")

            saveConnection.commit()

            logger.info("Done.")
            true
        } catch (e: SQLException) {
            logger.error("Failed to finalise the database: " + e.message)
            false
        } finally {
            closeConnection()
        }

}
