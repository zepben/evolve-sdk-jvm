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
 * Holds metadata about a run of an ingestor and metrics.
 *
 * @property jobId a unique UUID for the run.
 * @property source A string describing the source data for the run (e.g. "ExampleEnergy full HV/LV 2024-01-02 cut").
 * @property application The application used to ingest the source data.
 * @property applicationVersion The version of the ingestion application.
 * @property jobSources A map from strings identifying data sources (e.g. files) to their metadata.
 * @property networkMetrics A map from network containers to their metrics.
 */
data class IngestionMetrics(
    val jobId: UUID,
    val source: String,
    val application: String,
    val applicationVersion: String,
    val jobSources: JobSources = jobSourcesWithDefault(),
    val networkMetrics: NetworkMetrics = networkMetricsWithDefault()
)
