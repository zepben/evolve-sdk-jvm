/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common

import com.zepben.evolve.database.sqlite.cim.CimDatabaseReader
import com.zepben.evolve.database.sqlite.cim.metadata.MetadataCollectionReader
import com.zepben.evolve.database.sqlite.cim.tables.MissingTableConfigException
import com.zepben.evolve.database.sqlite.cim.tables.TableVersion
import com.zepben.testutils.junit.SystemLogExtension
import io.mockk.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.sql.Connection
import java.sql.Statement

internal class CimDatabaseReaderTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val databaseFile = "databaseFile"
    private val metadataReader = mockk<MetadataCollectionReader>().also { every { it.load() } returns true }
    private val serviceReader = mockk<BaseServiceReader>().also { every { it.load() } returns true }

    private val statement = mockk<Statement> { justRun { close() } }
    private val connection = mockk<Connection> {
        every { createStatement() } returns statement
        justRun { close() }
    }

    private val tableVersion = mockk<TableVersion> {
        every { getVersion(any()) } returns 1
        every { SUPPORTED_VERSION } returns 1
    }

    private var postLoadResult = true
    private var postLoadCalled = false

    private val reader = object : CimDatabaseReader(
        connection,
        metadataReader,
        serviceReader,
        mockk(), // Services won't be used as we have replaced the postLoad implementation. The real function is tested by each descendant class.
        databaseFile,
        tableVersion
    ) {
        override fun postLoad(): Boolean {
            postLoadCalled = true
            return postLoadResult
        }
    }

    @Test
    internal fun `can load from valid database`() {
        assertThat("Should have loaded", reader.load())

        verifyReadersCalled()
        assertThat("postLoad should have been called", postLoadCalled)
    }

    @Test
    fun `can only run once`() {
        assertThat("Should have loaded the first time", reader.load())
        assertThat("Shouldn't have loaded a second time", !reader.load())

        assertThat(systemErr.log, containsString("You can only use the database reader once."))
    }

    @Test
    internal fun `detect missing databases`() {
        every { connection.createStatement() } throws Exception("Test Error")

        assertThat("Should not have loaded", !reader.load())
        assertThat(systemErr.log, containsString("Failed to connect to the database for reading: Test Error"))

        verify { connection.createStatement() }
        confirmVerified(connection, metadataReader, serviceReader)
        assertThat("postLoad shouldn't have been called", !postLoadCalled)
    }

    @Test
    internal fun `detect old databases`() {
        every { tableVersion.getVersion(any()) } returns 0

        assertThat("Should not have loaded", !reader.load())
        assertThat(
            systemErr.log,
            containsString("Unable to load from database $databaseFile [found v0, expected v1]. Consider using the UpgradeRunner if you wish to support this database.")
        )

        verifyInvalidVersionCalls()
    }

    @Test
    internal fun `detect future databases`() {
        every { tableVersion.getVersion(any()) } returns 2

        assertThat("Should not have loaded", !reader.load())
        assertThat(
            systemErr.log,
            containsString("Unable to load from database $databaseFile [found v2, expected v1]. You need to use a newer version of the SDK to load this database.")
        )

        verifyInvalidVersionCalls()
    }

    @Test
    internal fun `detect invalid databases`() {
        every { tableVersion.getVersion(any()) } returns null

        assertThat("Should not have loaded", !reader.load())
        assertThat(systemErr.log, containsString("Failed to read the version number form the selected database. Are you sure it is a EWB database?"))

        verifyInvalidVersionCalls()
    }

    @Test
    internal fun `detect metadata failure`() {
        every { metadataReader.load() } returns false

        assertThat("Should not have loaded", !reader.load())

        verifyReadersCalled()
        assertThat("postLoad shouldn't have been called", !postLoadCalled)
    }

    @Test
    internal fun `detect reader failure`() {
        every { serviceReader.load() } returns false

        assertThat("Should not have loaded", !reader.load())

        verifyReadersCalled()
        assertThat("postLoad shouldn't have been called", !postLoadCalled)
    }

    @Test
    internal fun `detect postLoad failure`() {
        postLoadResult = false

        assertThat("Should not have loaded", !reader.load())

        verifyReadersCalled()
        assertThat("postLoad should have been called", postLoadCalled)
    }

    @Test
    internal fun `detect missing tables`() {
        every { serviceReader.load() } throws MissingTableConfigException("Test Error")

        assertThat("Should not have loaded", !reader.load())

        assertThat(systemErr.log, containsString("Unable to load database: Test Error"))
    }

    private fun verifyReadersCalled() {
        verifySequence {
            tableVersion.SUPPORTED_VERSION
            connection.createStatement()
            tableVersion.getVersion(statement)
            statement.close()

            metadataReader.load()
            serviceReader.load()
        }
    }

    private fun verifyInvalidVersionCalls() {
        verifySequence {
            tableVersion.SUPPORTED_VERSION
            connection.createStatement()
            tableVersion.getVersion(statement)
            statement.close()
        }

        confirmVerified(metadataReader, serviceReader)
        assertThat("postLoad shouldn't have been called", !postLoadCalled)
    }

}
