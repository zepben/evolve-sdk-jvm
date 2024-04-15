/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable
import com.zepben.evolve.database.sqlite.extensions.executeConfiguredQuery
import java.sql.SQLException
import java.sql.Statement

/**
 * Code representation of the `version` table.
 *
 * @property supportedVersion The supported schema version.
 *
 * @property VERSION Column definition.
 */
@Suppress("PropertyName")
abstract class TableVersion : SqliteTable() {

    abstract val supportedVersion: Int

    val VERSION: Column = Column(++columnIndex, "version", "TEXT", NOT_NULL)

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
