/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.metrics.IngestionMetrics
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.*

class MetricsDatabaseReader(
    private val connection: Connection
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun load(jobId: UUID): IngestionMetrics? {
        TODO("implement pls")
    }

}
