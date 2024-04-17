/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class TableVersionTest {

    private val resultSet = mockk<ResultSet> {
        every { isClosed } returns false
        justRun { fetchDirection = any() }
        every { next() } returns true
        every { getInt(1) } returns 1
        justRun { close() }
    }
    private val preparedStatement = mockk<PreparedStatement>().also {
        justRun { it.queryTimeout = any() }
        justRun { it.fetchSize = any() }
        justRun { it.close() }
        every { it.executeQuery() } returns resultSet
    }
    private val table = TableVersion(123)
    private val connection = mockk<Connection> {
        every { prepareStatement(table.selectSql) } returns preparedStatement
    }

    @Test
    internal fun `getVersion helper returns version from query`() {
        assertThat(table.getVersion(connection), equalTo(1))

        verify {
            preparedStatement.executeQuery()
            preparedStatement.close()
        }
    }

    @Test
    internal fun `getVersion helper detects failures`() {
        every { preparedStatement.executeQuery() } throws SQLException("test")

        assertThat(table.getVersion(connection), nullValue())

        verify {
            preparedStatement.executeQuery()
            preparedStatement.close()
        }
    }

}
