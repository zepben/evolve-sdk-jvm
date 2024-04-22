package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.metrics.IngestionJob
import com.zepben.evolve.metrics.IngestionMetadata
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
    private val metrics = IngestionJob(UUID.randomUUID(), metadata)
    private val metricsEntryWriter = mockk<MetricsEntryWriter> { every { save(any<IngestionMetadata>()) } returns true }
    private val metricsWriter = MetricsWriter(metrics, mockk(), metricsEntryWriter)

    @Test
    internal fun `passes objects through to the metrics entry writer`() {
        // NOTE: the save method will fail due to the relaxed mock returning false for all save operations,
        //       but a save should still be attempted on every object
        metricsWriter.save()

        verify(exactly = 1) { metricsEntryWriter.save(metadata) }
    }

}
