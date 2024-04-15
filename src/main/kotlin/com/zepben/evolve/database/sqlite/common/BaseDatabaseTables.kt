/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common

import com.zepben.evolve.database.sqlite.tables.MissingTableConfigException
import com.zepben.evolve.database.sqlite.tables.SqliteTable
import com.zepben.evolve.database.sqlite.tables.associations.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assetinfo.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.*
import java.sql.Connection
import java.sql.PreparedStatement
import kotlin.reflect.KClass

/**
 * The base collection of tables for all our databases.
 *
 * @property tables The tables that are available in this database, keyed on the table class. You should use [getTable] to access individual tables.
 * @property insertStatements A collection of [PreparedStatement] for each table, keyed on the table class. You should use [getInsert] to access individual inserts.
 * @property includedTables A sequence of [SqliteTable] indicating which tables are included in this database, which will be consumed to build the [tables]
 *   collection. NOTE: You should always append your tables to super.includedTables when overriding.
 */
open class BaseDatabaseTables : AutoCloseable {

    val tables: Map<KClass<out SqliteTable>, SqliteTable> by lazy { includedTables.associateBy { it::class } }
    var insertStatements: Map<KClass<out SqliteTable>, PreparedStatement> = mapOf()
        private set

    protected open val includedTables: Sequence<SqliteTable> = sequenceOf()

    /**
     * Helper function for getting the table of the specified type.
     *
     * @param T The type of table to get.
     * @return The requested table if it belongs to this database.
     * @throws MissingTableConfigException If the requested table doesn't belong to this database.
     */
    @Throws(MissingTableConfigException::class)
    inline fun <reified T : SqliteTable> getTable(): T {
        val table = tables[T::class]
            ?: throw MissingTableConfigException("INTERNAL ERROR: No table has been registered for ${T::class.simpleName}. You might want to consider fixing that.")

        return table as T
    }

    /**
     * Helper function for getting the insert statement for the specified table.
     *
     * @param T The type of table to get.
     * @return The insert statement for the requested table if it belongs to this database.
     * @throws MissingTableConfigException If the requested table doesn't belong to this database.
     */
    @Throws(MissingTableConfigException::class)
    inline fun <reified T : SqliteTable> getInsert(): PreparedStatement {
        return insertStatements[T::class]
            ?: throw MissingTableConfigException("INTERNAL ERROR: No prepared statement has been registered for ${T::class.simpleName}. You might want to consider fixing that.")
    }

    /**
     * Call the [action] on each table.
     *
     * @param action A callback invoked on each table.
     */
    fun forEachTable(action: (SqliteTable) -> Unit) {
        tables.values.forEach(action)
    }

    /**
     * Create a [PreparedStatement] for inserting into each table.
     *
     * @param connection The [Connection] to prepare the statements on.
     */
    fun prepareInsertStatements(connection: Connection) {
        closeInsertStatements()

        insertStatements = tables.mapValues { (_, table) -> connection.prepareStatement(table.preparedInsertSql) }
    }

    @Suppress("KDocMissingDocumentation")
    override fun close() {
        closeInsertStatements()
    }

    private fun closeInsertStatements() {
        insertStatements.forEach { (_, statement) -> statement.close() }
        insertStatements = mapOf()
    }

}
