/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables

/**
 * Represents a column in a database table.
 */
class Column @JvmOverloads constructor(
    val queryIndex: Int,
    val name: String,
    val type: String,
    val nullable: Nullable = Nullable.NONE
) {

    enum class Nullable {
        NONE, NOT_NULL, NULL;

        fun sqlString(): String = if (this == NULL) "NULL" else if (this == NOT_NULL) "NOT NULL" else ""
    }

    fun sqlString(): String = (name + " " + type + " " + nullable.sqlString()).trim { it <= ' ' }

    init {
        require(queryIndex >= 0) { "You cannot use a negative query indexes." }
    }

}
