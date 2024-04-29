/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.common.BaseCollectionWriter
import com.zepben.evolve.metrics.IngestionJob

/**
 * Class for writing an ingestion job (and associated metadata, metrics, and sources) to the tables in a metrics database.
 *
 * @param job The ingestion job to write.
 * @param databaseTables The tables in the database. Insert statements should be prepared for each table.
 * @param writer the [MetricsEntryWriter] to use for writing each entry.
 */
class MetricsWriter(
    private val job: IngestionJob,
    databaseTables: MetricsDatabaseTables,
    private val writer: MetricsEntryWriter = MetricsEntryWriter(databaseTables, job.id)
): BaseCollectionWriter() {

    override fun save(): Boolean = (job.metadata?.let { writer.save(it) } ?: false)
        .andSaveEach(job.sources.entries, writer::saveSource) { jobSource, e -> logger.error("Failed to save job source $jobSource: ${e.message}") }
        .andSaveEach(job.networkMetrics.entries, writer::saveMetric) { metric, e -> logger.error("Failed to save metric $metric: ${e.message}") }

}
