package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.metrics.*
import com.zepben.testutils.junit.SystemLogExtension
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
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

}
