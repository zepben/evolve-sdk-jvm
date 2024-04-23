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
 * A collection of network container metrics.
 *
 * @property entries Map entries from network containers to their metrics.
 */
class NetworkMetrics {

    private val containerToMetrics: MutableMap<NetworkContainer, NetworkContainerMetrics> = mutableMapOf()

    val entries: Set<NetworkMetric> get() = containerToMetrics.entries

    /**
     * Get metrics for a network container. This returns a new mutable map of metric names to values if there are no metrics for the specified container.
     *
     * @param container The container to fetch metrics for.
     * @return The metrics for [container].
     */
    operator fun get(container: NetworkContainer): NetworkContainerMetrics = containerToMetrics.getOrPut(container) { mutableMapOf() }

}
