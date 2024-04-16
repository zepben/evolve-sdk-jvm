package com.zepben.evolve.metrics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test

internal class SourceMetadataTest {

    private val sourceMetadata1 = SourceMetadata("ABC".toByteArray())
    private val sourceMetadata2 = SourceMetadata("ABC".toByteArray())
    private val sourceMetadata3 = SourceMetadata("XYZ".toByteArray())

    @Test
    internal fun equality() {
        assertThat(sourceMetadata1, equalTo(sourceMetadata2))
        assertThat(sourceMetadata1, not(equalTo(sourceMetadata3)))
    }

    @Test
    internal fun hash() {
        assertThat(sourceMetadata1.hashCode(), equalTo(sourceMetadata2.hashCode()))
        assertThat(sourceMetadata1.hashCode(), not(equalTo(sourceMetadata3.hashCode())))
    }

}
