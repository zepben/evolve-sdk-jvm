/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.paths

enum class DatabaseType(val perDate: Boolean, val fileDescriptor: String)
{
    CUSTOMER(perDate = true, fileDescriptor="customers"),
    DIAGRAM(perDate = true, fileDescriptor="diagrams"),
    MEASUREMENT(perDate = true, fileDescriptor="measurements"),
    NETWORK_MODEL(perDate = true, fileDescriptor="network-model"),
    TILE_CACHE(perDate = true, fileDescriptor="tile-cache"),
    ENERGY_READING(perDate = true, fileDescriptor="load-readings"),

    ENERGY_READINGS_INDEX(perDate = false, fileDescriptor="load-readings-index"),
    LOAD_AGGREGATOR_METERS_BY_DATE(perDate = false, fileDescriptor="load-aggregator-mbd"),
    WEATHER_READING(perDate = false, fileDescriptor="weather-readings"),
    RESULTS_CACHE(perDate = false, fileDescriptor="results-cache"),
    METRICS(perDate = false, fileDescriptor = "metrics")
}
