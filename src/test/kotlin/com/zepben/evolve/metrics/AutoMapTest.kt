/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test

class AutoMapTest {

    val autoMap = object : AutoMap<String, MutableList<Int>>() {
        override fun defaultValue(): MutableList<Int> = mutableListOf(5)
    }

    @Test
    fun defaultValue() {
        assertThat(autoMap["abc"], contains(5))
    }

    @Test
    fun getEntries() {
        assertThat(autoMap.entries, empty())
        autoMap["abc"]
        autoMap["xyz"].add(6)
        assertThat(
            autoMap.entries.map { it.key to it.value },
            contains("abc" to listOf(5), "xyz" to listOf(5, 6))
        )
    }

    @Test
    fun getKeys() {
        assertThat(autoMap.keys, empty())
        autoMap["abc"]
        autoMap["xyz"].add(6)
        assertThat(autoMap.keys, containsInAnyOrder("abc", "xyz"))
    }

    @Test
    fun getSize() {
        assertThat(autoMap.size, equalTo(0))
        autoMap["abc"]
        autoMap["xyz"].add(6)
        assertThat(autoMap.size, equalTo(2))
    }

    @Test
    fun getValues() {
        assertThat(autoMap.values, empty())
        autoMap["abc"]
        autoMap["xyz"].add(6)
        assertThat(autoMap.values, containsInAnyOrder(listOf(5), listOf(5, 6)))
    }

    @Test
    fun isEmpty() {
        assertThat("AutoMap should start empty", autoMap.isEmpty())
        autoMap["abc"]
        assertThat("AutoMap should not be empty after first access", autoMap.isNotEmpty())
    }

    @Test
    fun get() {
        autoMap["xyz"].add(6)
        assertThat(autoMap["abc"], contains(5))
        assertThat(autoMap["xyz"], contains(5, 6))
    }

    @Test
    fun containsValue() {
        assertThat("AutoMap does not contain default value before access", not(autoMap.containsValue(listOf(5))))
        autoMap["abc"]
        autoMap["xyz"].add(6)
        assertThat("AutoMap contains default value after access", autoMap.containsValue(listOf(5)))
        assertThat("AutoMap contains modified value", autoMap.containsValue(listOf(5, 6)))
    }

    @Test
    fun containsKey() {
        assertThat("AutoMap does not contain key before access", not(autoMap.containsKey("abc")))
        autoMap["abc"]
        assertThat("AutoMap does contains key after access", not(autoMap.containsKey("abc")))
    }
}
