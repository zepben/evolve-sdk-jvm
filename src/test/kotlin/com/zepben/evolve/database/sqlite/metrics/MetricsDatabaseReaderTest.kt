/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.TableVersion
import com.zepben.evolve.database.sqlite.metrics.tables.TableJobs
import com.zepben.evolve.metrics.IngestionMetadata
import com.zepben.evolve.metrics.IngestionMetrics
import com.zepben.testutils.junit.SystemLogExtension
import io.mockk.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import java.util.*

internal class MetricsDatabaseReaderTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val uuid = UUID.fromString("224bcd90-b37f-4f93-8b4f-3c9819b4d855")

    private val tableVersion = mockk<TableVersion> {
        every { getVersion(any()) } returns 1
        every { supportedVersion } returns 1
    }

    private val tableJobs = TableJobs()
    private val tables = mockk<MetricsDatabaseTables> {
        every { tables } returns mapOf(TableVersion::class to tableVersion, TableJobs::class to tableJobs)
    }

    private val metadata = IngestionMetadata(Instant.EPOCH, "source", "application", "applicationVersion")

    private val jobResultSet = mockk<ResultSet> {
        every { next() } returns true
        every { wasNull() } returns false
        justRun { close() }
        every { getString(tableJobs.JOB_ID.queryIndex) } returns "224bcd90-b37f-4f93-8b4f-3c9819b4d855"
        every { getString(tableJobs.INGEST_TIME.queryIndex) } returns metadata.startTime.toString()
        every { getString(tableJobs.SOURCE.queryIndex) } returns metadata.source
        every { getString(tableJobs.APPLICATION.queryIndex) } returns metadata.application
        every { getString(tableJobs.APPLICATION_VERSION.queryIndex) } returns metadata.applicationVersion
    }

    private val getJobStatement = mockk<PreparedStatement> {
        justRun { setString(1, "224bcd90-b37f-4f93-8b4f-3c9819b4d855") }
        justRun { close() }
        every { executeQuery() } returns jobResultSet
    }

    private val connection = mockk<Connection> {
        justRun { close() }
        every { prepareStatement(any()) } returns getJobStatement
    }

    private val reader = MetricsDatabaseReader(
        connection,
        tables
    )

    @BeforeEach
    private fun mockReader() {
        mockkConstructor(MetricsReader::class)
        every {
            constructedWith<MetricsReader>(
                FunctionMatcher<IngestionMetrics>({ it.jobId == uuid && it.metadata == metadata }, IngestionMetrics::class),
                EqMatcher(tables),
                EqMatcher(connection)
            ).load()
        } returns true
    }

    @Test
    internal fun `can load from valid database`() {
        val metrics = reader.load(uuid)!!
        MatcherAssert.assertThat(metrics.jobId, equalTo(uuid))
        MatcherAssert.assertThat(metrics.metadata, equalTo(metadata))
    }

}
