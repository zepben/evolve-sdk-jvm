/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import java.util.*

class IngestionMetricsCollection {

    private val metricsByUUID: MutableMap<UUID, IngestionMetrics> = mutableMapOf()

    fun oldestFirst(): List<IngestionMetrics> = metricsByUUID.values.sortedWith(compareBy(nullsLast()) { it.metadata?.startTime })

    fun newestFirst(): List<IngestionMetrics> = metricsByUUID.values.sortedWith(compareByDescending(nullsLast()) { it.metadata?.startTime })

    fun add(metrics: IngestionMetrics) {
        metricsByUUID[metrics.jobId] = metrics
    }

    fun remove(uuid: UUID) = metricsByUUID.remove(uuid)

    operator fun get(jobId: UUID): IngestionMetrics = metricsByUUID.getOrPut(jobId) { IngestionMetrics(jobId) }
    operator fun get(jobId: String) = get(UUID.fromString(jobId))

    operator fun plusAssign(metrics: IngestionMetrics) = add(metrics)

}
