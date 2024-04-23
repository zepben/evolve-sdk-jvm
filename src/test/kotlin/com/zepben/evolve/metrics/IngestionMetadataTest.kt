package com.zepben.evolve.metrics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.time.Instant

internal class IngestionMetadataTest {

    @Test
    internal fun constructorCoverage() {
        val metadata = IngestionMetadata(Instant.EPOCH, "source", "application", "applicationVersion")

        assertThat(metadata.startTime, equalTo(Instant.EPOCH))
        assertThat(metadata.source, equalTo("source"))
        assertThat(metadata.application, equalTo("application"))
        assertThat(metadata.applicationVersion, equalTo("applicationVersion"))
    }

}
