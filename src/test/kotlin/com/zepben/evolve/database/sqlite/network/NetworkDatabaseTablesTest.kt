/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.network

import com.zepben.evolve.database.sqlite.common.BaseDatabaseTables
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class NetworkDatabaseTablesTest {

    @Test
    fun `contains base tables`() {
        val tables = NetworkDatabaseTables().tables.keys
        val baseTables = (object : BaseDatabaseTables() {}).tables.keys

        assertThat("should contain all base tables", tables.containsAll(baseTables))
    }

}
