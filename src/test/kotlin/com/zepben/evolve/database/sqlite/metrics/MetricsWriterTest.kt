/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.metrics.*
import com.zepben.testutils.junit.SystemLogExtension
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.sqlite.SQLiteErrorCode
import org.sqlite.SQLiteException
import java.time.Instant
import java.util.*

internal class MetricsWriterTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val metadata = IngestionMetadata(Instant.EPOCH, "N/A", "test", "0.0.0")
    private val job = IngestionJob(UUID.randomUUID(), metadata).apply {
        sources["abc"]
        networkMetrics[TotalNetworkContainer]
    }
    private val metricsEntryWriter = mockk<MetricsEntryWriter> {
        every { save(any<IngestionMetadata>()) } returns true
        every { saveSource(any<JobSource>()) } returns true
        every { saveMetric(any<NetworkMetric>()) } returns true
    }
    private val metricsWriter = MetricsWriter(job, mockk(), metricsEntryWriter)

    @Test
    internal fun `passes objects through to the metrics entry writer`() {
        metricsWriter.save()

        verify(exactly = 1) {
            metricsEntryWriter.save(metadata)
            metricsEntryWriter.saveSource(job.sources.entries.first())
            metricsEntryWriter.saveMetric(job.networkMetrics.entries.first())
        }
    }

    @Test
    internal fun `source failure message`() {
        every { metricsEntryWriter.saveSource(any<JobSource>()) } throws SQLiteException("message", SQLiteErrorCode.SQLITE_ERROR)
        metricsWriter.save()

        assertThat(systemErr.log, containsString("Failed to save job source"))
        assertThat(systemErr.log, containsString("message"))
    }

    @Test
    internal fun `metric failure message`() {
        every { metricsEntryWriter.saveMetric(any<NetworkMetric>()) } throws SQLiteException("message", SQLiteErrorCode.SQLITE_ERROR)
        metricsWriter.save()

        assertThat(systemErr.log, containsString("Failed to save metric"))
        assertThat(systemErr.log, containsString("message"))
    }

    @Test
    internal fun `save returns false if missing job metadata`() {
        every { metricsEntryWriter.save(any<IngestionMetadata>()) } returns false
        assertThat("Ingestion job without metadata should not save", !metricsWriter.save())
    }

}
