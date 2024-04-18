/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import java.util.*

typealias IngestionMetricsCollection = MutableMap<UUID, IngestionMetrics>

fun IngestionMetricsCollection.oldestFirst(): List<IngestionMetrics> = values.sortedBy { it.metadata.startTime }
fun IngestionMetricsCollection.newestFirst(): List<IngestionMetrics> = values.sortedByDescending { it.metadata.startTime }
