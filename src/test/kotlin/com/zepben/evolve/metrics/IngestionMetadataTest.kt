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
