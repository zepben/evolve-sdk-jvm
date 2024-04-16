/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.BaseCollectionWriter
import com.zepben.evolve.metrics.IngestionMetrics

class MetricsWriter(
    private val metrics: IngestionMetrics,
    databaseTables: MetricsDatabaseTables,
    private val writer: MetricsEntryWriter = MetricsEntryWriter(databaseTables, metrics.jobId)
): BaseCollectionWriter() {

    override fun save(): Boolean = writer.save(metrics.metadata)
        .andSaveEach(metrics.jobSources.entries, writer::save) { jobSource, e -> logger.error("Failed to save job source $jobSource: ${e.message}")}
        .andSaveEach(metrics.networkMetrics.entries, writer::save) { metric, e -> logger.error("Failed to save metric $metric: ${e.message}") }

}
