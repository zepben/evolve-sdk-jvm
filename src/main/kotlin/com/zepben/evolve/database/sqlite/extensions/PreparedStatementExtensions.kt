/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite.extensions

import java.lang.Double.isNaN
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import java.sql.PreparedStatement
import java.sql.Types
import java.sql.Types.DOUBLE
import java.time.Instant

internal fun PreparedStatement.setNullableString(queryIndex: Int, value: String?) {
    when (value) {
        null -> setNull(queryIndex, Types.VARCHAR)
        else -> setString(queryIndex, value)
    }
}

internal fun PreparedStatement.setNullableDouble(queryIndex: Int, value: Double?) {
    when {
        value == null -> this.setNull(queryIndex, DOUBLE)
        isNaN(value) -> this.setString(queryIndex, "NaN")
        else -> this.setDouble(queryIndex, value)
    }
}

internal fun PreparedStatement.setNullableInt(queryIndex: Int, value: Int?) {
    if (value == null)
        this.setNull(queryIndex, Types.INTEGER)
    else
        this.setInt(queryIndex, value)
}

internal fun PreparedStatement.setNullableLong(queryIndex: Int, value: Long?) {
    if (value == null)
        this.setNull(queryIndex, Types.INTEGER)
    else
        this.setLong(queryIndex, value)
}

internal fun PreparedStatement.setInstant(queryIndex: Int, value: Instant?) {
    when (value) {
        null -> setNull(queryIndex, Types.VARCHAR)
        else -> setString(queryIndex, value.toString())
    }
}

internal fun PreparedStatement.executeSingleUpdate(): Boolean {
    return executeUpdate() == 1
}

internal fun PreparedStatement.sql(): String {
    return try {
        AccessController.doPrivileged(PrivilegedExceptionAction {
            val field = javaClass.getFieldExt("sql")
            field.isAccessible = true
            field[this].toString()
        } as PrivilegedExceptionAction<String>)
    } catch (e: PrivilegedActionException) {
        "Failed to extract SQL - " + e.message
    }
}

internal fun PreparedStatement.parameters(): String {
    return try {
        AccessController.doPrivileged(PrivilegedExceptionAction {
            val field = javaClass.getFieldExt("batch")
            field.isAccessible = true
            (field[this] as Array<*>).contentToString()
        } as PrivilegedExceptionAction<String>)
    } catch (e: Exception) {
        "Failed to extract parameters - " + e.message
    }
}
