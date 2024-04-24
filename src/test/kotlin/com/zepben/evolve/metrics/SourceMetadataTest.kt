/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import java.time.Instant

internal class SourceMetadataTest {

    private val sourceMetadata1 = SourceMetadata(Instant.EPOCH, "ABC".toByteArray())
    private val sourceMetadata2 = SourceMetadata(Instant.EPOCH, "ABC".toByteArray())
    private val sourceMetadata3 = SourceMetadata(Instant.EPOCH, "XYZ".toByteArray())
    private val sourceMetadata4 = SourceMetadata(Instant.EPOCH.plusSeconds(1), "ABC".toByteArray())

    @Test
    internal fun equality() {
        assertThat(sourceMetadata1, equalTo(sourceMetadata2))
        assertThat(sourceMetadata1, not(equalTo(sourceMetadata3)))
        assertThat(sourceMetadata1, not(equalTo(sourceMetadata4)))
        assertThat(SourceMetadata(), equalTo(SourceMetadata()))
        assertThat(SourceMetadata(), not(equalTo(SourceMetadata(fileHash = "ABC".toByteArray()))))
    }

    @Test
    internal fun hash() {
        assertThat(sourceMetadata1.hashCode(), equalTo(sourceMetadata2.hashCode()))
        assertThat(sourceMetadata1.hashCode(), not(equalTo(sourceMetadata3.hashCode())))
        assertThat(sourceMetadata1.hashCode(), not(equalTo(sourceMetadata4.hashCode())))
    }

    @Test
    internal fun setters() {
        sourceMetadata1.timestamp = Instant.EPOCH.plusSeconds(123)
        sourceMetadata1.fileHash = "123".toByteArray()
        assertThat(sourceMetadata1.timestamp, equalTo(Instant.EPOCH.plusSeconds(123)))
        assertThat(sourceMetadata1.fileHash, equalTo("123".toByteArray()))
    }

}
