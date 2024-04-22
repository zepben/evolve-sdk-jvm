package com.zepben.evolve.metrics

import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import java.util.*

internal class IngestionJobTest {

    private val uuid = UUID.randomUUID()

    @Test
    internal fun constructorCoverage() {
        val job = IngestionJob(uuid)
        assertThat(job.id, equalTo(uuid))
        assertThat(job.metadata, nullValue())
        assertThat(job.sources.entries, empty())
        assertThat(job.networkMetrics.entries, empty())
    }

    @Test
    internal fun metadata() {
        val job = IngestionJob(uuid)
        val metadata = mockk<IngestionMetadata>()
        job.metadata = metadata
        assertThat(job.metadata, equalTo(metadata))
    }

}
