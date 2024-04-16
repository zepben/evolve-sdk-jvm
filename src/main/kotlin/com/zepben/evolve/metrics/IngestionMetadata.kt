/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import java.time.Instant

/**
 * Metadata for an ingestion run.
 *
 * @property startTime Timestamp for when the run started.
 * @property source A string describing the source data for the run (e.g. "ExampleEnergy full HV/LV 2024-01-02 cut").
 * @property application The application used to ingest the source data.
 * @property applicationVersion The version of the ingestion application.
 */
data class IngestionMetadata(
    val startTime: Instant,
    val source: String,
    val application: String,
    val applicationVersion: String
)
