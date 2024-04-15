/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.tables.metrics

import com.zepben.evolve.database.sqlite.extensions.executeConfiguredQuery
import com.zepben.evolve.database.sqlite.tables.Column
import com.zepben.evolve.database.sqlite.tables.SqliteTable
import java.sql.SQLException
import java.sql.Statement

/**
 * Code representation of the `version` table in the metrics database.
 *
 * @property SUPPORTED_VERSION The supported schema version.
 *
 * @property VERSION Column definition.
 */
class TableMetricsVersion : SqliteTable() {

    val SUPPORTED_VERSION: Int = 1

    val VERSION: Column = Column(++columnIndex, "version", "TEXT", Column.Nullable.NOT_NULL)

    override val name: String = "version"

    /**
     * Helper function to read the version from the database.
     */
    @Throws(SQLException::class)
    fun getVersion(statement: Statement): Int? =
        runCatching {
            statement.executeConfiguredQuery(selectSql).use { results ->
                results.next()
                results.getInt(VERSION.queryIndex)
            }
        }.getOrNull()

}
