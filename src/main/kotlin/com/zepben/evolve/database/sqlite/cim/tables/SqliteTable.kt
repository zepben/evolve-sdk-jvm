/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables

import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.kotlinProperty

/**
 * Represents a table in a Sqlite Database
 *
 * @property name The name of the table in the actual database.
 * @property description Readable description of the contents of the table for adding to logs.
 * @property createTableSql The SQL statement that should be executed to create the table in the database.
 * @property preparedInsertSql The SQL statement that should be used with a [PreparedStatement] to insert entries into the table.
 * @property createIndexesSql The SQL statement that should be executed to create the indexes for the table in the database. Should be executed after all
 *   entries are inserted into the table.
 * @property selectSql The SQL statement that should be used to read the entries from the table in the database. This value is `open` to allow descendant
 *   classes to modify the SQL to add things such as ordering when that is important.
 * @property preparedUpdateSql The SQL statement that should be used with a [PreparedStatement] to update entries into the table.
 */
abstract class SqliteTable {

    //
    // NOTE: `name` is defined as an abstract val, rather than a class member via the constructor, to avoid passing it between all abstract classes in the
    // table class hierarchy, which removes a lot of boilerplate.
    //
    abstract val name: String

    open val description: String by lazy { name.replace('_', ' ') }

    val createTableSql: String by lazy { "CREATE TABLE $name (${columnSet.joinToString { it.sqlString() }})" }

    val preparedInsertSql: String by lazy { "INSERT INTO $name (${columnNames.joinToString()}) VALUES (${columnNames.joinToString { "?" }})" }

    val createIndexesSql: Collection<String> by lazy { uniqueIndexColumns.toCreateIndexSql(true) + nonUniqueIndexColumns.toCreateIndexSql(false) }

    open val selectSql: String by lazy { "SELECT ${columnNames.joinToString()} FROM $name" }

    val preparedUpdateSql: String by lazy { "UPDATE $name SET ${columnNames.joinToString { "$it = ?" }}" }

    /**
     * The index of the previously added column. Should be used to increment column indexes to avoid massive rework when adding columns to base classes.
     *
     * e.g.
     * val MY_COL: Column = Column(++columnIndex, "my_col", "TEXT", NOT_NULL)
     */
    protected var columnIndex: Int = 0

    /**
     * A list of column groups that require a unique index in the database.
     */
    protected open val uniqueIndexColumns: MutableList<List<Column>> = mutableListOf()

    /**
     * A list of column groups that require a non-unique index in the database.
     */
    protected open val nonUniqueIndexColumns: MutableList<List<Column>> = mutableListOf()

    private val logger = LoggerFactory.getLogger(javaClass)

    private val columnSet: SortedSet<Column> by lazy { createColumnSet(javaClass, this) }
    private val columnNames: List<String> by lazy { columnSet.map { it.name } }

    @Suppress("UNCHECKED_CAST")
    private fun createColumnSet(clazz: Class<*>, instance: SqliteTable): SortedSet<Column> {
        // We sort by the queryIndex so insert and select statements can be addressed by a number
        val cols = sortedSetOf<Column>(Comparator.comparing { it.queryIndex })
        var repeatedField: Boolean

        if (clazz.superclass != null)
            cols.addAll(createColumnSet(clazz.superclass, instance))

        for (field in clazz.declaredFields) {
            if (field.type == Column::class.java) {
                try {
                    repeatedField = !cols.add(field.get(instance) as Column)
                    if (repeatedField)
                        logger.error("INTERNAL ERROR: The field ${field.name} in the SQL table class ${clazz.name} is using an index that has already been used. Did you forget a ++?")
                } catch (e: IllegalAccessException) {
                    logger.debug("Trying to retrieve field ${field.name}as Kotlin property.")
                    try {
                        val prop = field.kotlinProperty as? KProperty1<Any, Column>
                        if (prop == null) {
                            logger.error("INTERNAL ERROR: The field ${field.name} in the SQL table class ${clazz.name} couldn't be casted as a KProp as its kotlinProperty was null. It will be missing from the database.")
                            continue
                        }

                        repeatedField = !cols.add(prop.get(instance))
                        if (repeatedField)
                            logger.error("INTERNAL ERROR: The field ${field.name} in the SQL table class ${clazz.name} is using an index that has already been used. Did you forget a ++?")
                    } catch (e: IllegalAccessException) {
                        logger.error("Unable to retrieve field ${field.name}. It will be missing from the database: ${e.message}")
                    }
                }
            }
        }
        return Collections.unmodifiableSortedSet(cols)
    }

    private fun MutableList<List<Column>>.toCreateIndexSql(isUnique: Boolean): List<String> =
        map { indexCols ->
            "CREATE ${if (isUnique) "UNIQUE " else ""}INDEX " +
                "${name}_${indexCols.joinToString("_") { it.name }} " +
                "ON $name (${indexCols.joinToString { it.name }})"
        }

}
