/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace

import com.zepben.evolve.services.network.tracing.traversals.Tracker

class NetworkTraceTracker<T, K>(
    private val keyExtractor: (NetworkTraceStep<T>) -> K,
) : Tracker<NetworkTraceStep<T>> {
    private val visited = mutableSetOf<K>()

    override fun hasVisited(item: NetworkTraceStep<T>): Boolean = visited.contains(keyExtractor(item))

    override fun visit(item: NetworkTraceStep<T>): Boolean = visited.add(keyExtractor(item))

    override fun clear() {
        visited.clear()
    }
}