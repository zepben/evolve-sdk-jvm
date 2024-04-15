/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common

import com.zepben.evolve.database.sqlite.extensions.executeConfiguredQuery
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

/**
 * A base class for reading collections of object collections from a database.
 *
 * @property databaseTables The tables that are available in the database
 * @property logger The logger to use for this collection reader.

 * @param connection The connection to the database to read.
 */
abstract class BaseCollectionReader(
    val databaseTables: BaseDatabaseTables,
    private val connection: Connection
) {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Load all the objects for the available collections.
     *
     * @return true if the load was successful, otherwise false.
     */
    abstract fun load(): Boolean

    /**
     * Helper function for chaining [loadEach] calls using the [and] operator in a more readable manner.
     *
     * @param T The [SqliteTable] to read the objects from.
     * @param processRow A callback for processing each row in the table. The callback will be provided with the table, the results for the row and a callback
     *   to set the identifier for the row, which returns the same value, so it can be used fluently.
     */
    protected inline fun <reified T : SqliteTable> Boolean.andLoadEach(
        crossinline processRow: (T, ResultSet, setIdentifier: (String) -> String) -> Boolean
    ): Boolean =
        this and loadEach(processRow)

    /**
     * Load each row of a table.
     *
     * @param T The [SqliteTable] to read the objects from.
     * @param processRow A callback for processing each row in the table. The callback will be provided with the table, the results for the row and a callback
     *   to set the identifier for the row, which returns the same value, so it can be used fluently.
     */
    protected inline fun <reified T : SqliteTable> loadEach(
        crossinline processRow: (T, ResultSet, setIdentifier: (String) -> String) -> Boolean
    ): Boolean {
        return databaseTables.getTable<T>().loadAll { results ->
            var lastIdentifier: String? = null
            val setIdentifier = { identifier: String -> lastIdentifier = identifier; identifier }

            try {
                var count = 0
                while (results.next()) {
                    if (processRow(this, results, setIdentifier)) {
                        ++count
                    }
                }

                return@loadAll count
            } catch (e: SQLException) {
                logger.error("Failed to load '$lastIdentifier' from '$name': ${e.message}")
                throw e
            }
        }
    }

    /**
     * You really shouldn't need to use this function directly, use [loadEach] instead.
     *
     * NOTE: This is marked protected rather than private to allow the inline reified functions above to work.
     */
    protected fun <T : SqliteTable> T.loadAll(processRows: T.(ResultSet) -> Int): Boolean {
        logger.info("Loading $description...")

        val thrown = try {
            val count = connection.createStatement().use { statement ->
                statement.executeConfiguredQuery(selectSql).use { results ->
                    processRows(results)
                }
            }
            logger.info("Successfully loaded $count $description.")
            return true
        } catch (t: Throwable) {
            when (t) {
                is SQLException,
                is IllegalArgumentException,
                is MRIDLookupException,
                is DuplicateMRIDException -> t

                else -> throw t
            }
        }

        logger.error("Failed to read the $description from '$name': ${thrown.message}", thrown)
        return false
    }

}
