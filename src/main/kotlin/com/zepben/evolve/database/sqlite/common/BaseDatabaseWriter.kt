/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common

import com.zepben.evolve.database.sqlite.cim.tables.MissingTableConfigException
import com.zepben.evolve.database.sqlite.extensions.configureBatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

/**
 * A base class for writing objects to one of our databases.
 *
 * @param databaseTables The tables to create in the database.
 * @param getConnection Provider of the connection to the specified database.
 *
 * @property logger The logger to use for this database writer.
 */
abstract class BaseDatabaseWriter(
    protected val databaseTables: BaseDatabaseTables,
    private val getConnection: () -> Connection
) {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    protected lateinit var saveConnection: Connection
    private var hasBeenUsed: Boolean = false

    /**
     * Save the database using the [saveSchema] method.
     *
     * @return true if the database was successfully saved, otherwise false.
     */
    fun save(): Boolean {
        if (hasBeenUsed) {
            logger.error("You can only use the database writer once.")
            return false
        }
        hasBeenUsed = true

        val setupStatus = preConnect() && connect() && preSave() && prepareInsertStatements()
        if (!setupStatus) {
            closeConnection()
            return false
        }

        val status = try {
            saveSchema()
        } catch (e: MissingTableConfigException) {
            logger.error("Unable to save database: " + e.message, e)
            false
        } and postSave()

        closeConnection()

        return status
    }
    
    abstract fun saveSchema(): Boolean

    open fun preConnect(): Boolean = true

    open fun preSave(): Boolean = true

    open fun postSave(): Boolean = true

    private fun connect(): Boolean =
        try {
            saveConnection = getConnection().configureBatch()
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

    private fun closeConnection() {
        try {
            if (::saveConnection.isInitialized)
                saveConnection.close()
        } catch (e: SQLException) {
            logger.error("Failed to close connection to database: " + e.message)
        }
    }

}
