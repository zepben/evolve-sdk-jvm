/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import java.util.*

class IngestionJobCollection: Collection<IngestionJob> {

    private val jobsByUUID: MutableMap<UUID, IngestionJob> = mutableMapOf()

    fun oldestFirst(): List<IngestionJob> = jobsByUUID.values.sortedWith(compareBy(nullsLast()) { it.metadata?.startTime })

    fun newestFirst(): List<IngestionJob> = jobsByUUID.values.sortedWith(compareByDescending(nullsLast()) { it.metadata?.startTime })

    fun add(metrics: IngestionJob) {
        jobsByUUID[metrics.id] = metrics
    }

    fun remove(uuid: UUID) = jobsByUUID.remove(uuid)

    operator fun contains(jobId: UUID): Boolean = jobsByUUID.containsKey(jobId)

    operator fun contains(jobId: String) = contains(UUID.fromString(jobId))

    operator fun get(jobId: UUID): IngestionJob = jobsByUUID.getOrPut(jobId) { IngestionJob(jobId) }
    operator fun get(jobId: String) = get(UUID.fromString(jobId))

    operator fun plusAssign(metrics: IngestionJob) = add(metrics)

    override val size: Int = jobsByUUID.size
    override fun isEmpty(): Boolean = jobsByUUID.isEmpty()

    override fun iterator(): Iterator<IngestionJob> = jobsByUUID.values.iterator()

    override fun containsAll(elements: Collection<IngestionJob>): Boolean = jobsByUUID.values.containsAll(elements)

    override fun contains(element: IngestionJob): Boolean = contains(element.id)

}
