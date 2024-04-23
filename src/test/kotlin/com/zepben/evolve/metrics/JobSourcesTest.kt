package com.zepben.evolve.metrics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

internal class JobSourcesTest {

    @Test
    internal fun defaultValue() {
        val sources = JobSources()
        assertThat(sources["abc"], equalTo(SourceMetadata()))
    }

}
