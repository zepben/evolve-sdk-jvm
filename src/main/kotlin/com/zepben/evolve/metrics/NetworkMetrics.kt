/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

typealias NetworkMetric = Map.Entry<NetworkContainer, NetworkContainerMetrics>

class NetworkMetrics {

    private val containerToMetrics: MutableMap<NetworkContainer, NetworkContainerMetrics> = mutableMapOf()

    val entries: Set<NetworkMetric> get() = containerToMetrics.entries

    operator fun get(container: NetworkContainer) = containerToMetrics.getOrPut(container) { mutableMapOf() }

}
