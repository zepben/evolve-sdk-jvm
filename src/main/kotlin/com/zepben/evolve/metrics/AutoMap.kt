/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

abstract class AutoMap<K, V> : Map<K, V> {

    private val map: MutableMap<K, V> = mutableMapOf()

    /**
     * Factory function to use when creating a value for a new key.
     */
    abstract fun defaultValue(): V

    override val entries: Set<Map.Entry<K, V>> = map.entries
    override val keys: Set<K> = map.keys
    override val size: Int get() = map.size
    override val values: Collection<V> = map.values

    override fun isEmpty(): Boolean = map.isEmpty()

    /**
     * Get value for [key] if it exists in this map, or create and put a default value.
     */
    override fun get(key: K): V = map.getOrPut(key, ::defaultValue)

    override fun containsValue(value: V): Boolean = map.containsValue(value)

    override fun containsKey(key: K): Boolean = map.containsKey(key)

}
