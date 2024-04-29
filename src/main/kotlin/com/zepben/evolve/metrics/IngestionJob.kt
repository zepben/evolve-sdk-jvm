/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import java.util.*

/**
 * Represents a single run of an ingestor.
 *
 * @property id A unique UUID for the run.
 * @property metadata Metadata for the run.
 * @property sources A map from data sources identifiers to their metadata.
 * @property networkMetrics A map from network containers to their metrics.
 */
data class IngestionJob @JvmOverloads constructor(
    val id: UUID,
    var metadata: IngestionMetadata? = null,
    val sources: JobSources = JobSources(),
    val networkMetrics: NetworkMetrics = NetworkMetrics()
)
