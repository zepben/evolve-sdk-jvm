/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables

import com.zepben.evolve.database.sqlite.common.TableVersion
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class TableVersionTest {

    private val resultSet = mockk<ResultSet> {
        every { isClosed } returns false
        justRun { fetchDirection = any() }
        every { next() } returns true
        every { getInt(1) } returns 1
        justRun { close() }
    }
    private val statement = mockk<Statement>().also {
        justRun { it.queryTimeout = any() }
        justRun { it.fetchSize = any() }
        every { it.executeQuery(any()) } returns resultSet
    }

    private val table = object : TableVersion() {
        override val supportedVersion: Int = 123
    }

    @Test
    internal fun `getVersion helper returns version from query`() {
        assertThat(table.getVersion(statement), equalTo(1))

        verify { statement.executeQuery(table.selectSql) }
    }

    @Test
    internal fun `getVersion helper detects failures`() {
        every { statement.executeQuery(any()) } throws SQLException("test")

        assertThat(table.getVersion(statement), nullValue())

        verify { statement.executeQuery(table.selectSql) }
    }

}
