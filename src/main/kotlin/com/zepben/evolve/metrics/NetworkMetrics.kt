/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

/**
 * A map from metric names to their values.
 */
typealias NetworkContainerMetrics = MutableMap<String, Double>

/**
 * Type holding a network container (partial or total) and its corresponding metrics.
 */
typealias NetworkMetric = Map.Entry<NetworkContainer, NetworkContainerMetrics>

/**
 * A collection of network container metrics. Missing metric maps are automatically created.
 */
class NetworkMetrics : AutoMap<NetworkContainer, NetworkContainerMetrics>() {

    override fun defaultValue(): NetworkContainerMetrics = mutableMapOf()

}
