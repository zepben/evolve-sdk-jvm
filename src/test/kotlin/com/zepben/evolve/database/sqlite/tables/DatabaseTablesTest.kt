/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.tables

import com.google.common.reflect.ClassPath
import com.zepben.evolve.database.sqlite.common.cim.CimDatabaseTables
import com.zepben.evolve.database.sqlite.customer.CustomerDatabaseTables
import com.zepben.evolve.database.sqlite.diagram.DiagramDatabaseTables
import com.zepben.evolve.database.sqlite.network.NetworkDatabaseTables
import com.zepben.testutils.exception.ExpectException.Companion.expect
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier

internal class DatabaseTablesTest {

    private val tables = CimDatabaseTables()

    @Test
    internal fun `all tables are used by at least one database`() {
        val allFinalTables = ClassPath.from(ClassLoader.getSystemClassLoader())
            .getTopLevelClassesRecursive("com.zepben.evolve.database.sqlite.tables")
            .asSequence()
            .map { it.load() }
            .filter { !Modifier.isAbstract(it.modifiers) }
            .filter { SqliteTable::class.java.isAssignableFrom(it) }
            .map { it.simpleName }
            .toSet()

        val usedTables = sequenceOf(CustomerDatabaseTables(), DiagramDatabaseTables(), NetworkDatabaseTables())
            .flatMap { it.tables.keys }
            .map { it.simpleName!! }
            .toSet()

        assertThat(usedTables, equalTo(allFinalTables))
    }

    @Test
    internal fun `throws on missing tables`() {
        expect { tables.getTable<MissingTable>() }.toThrow<MissingTableConfigException>()
    }

    private class MissingTable : SqliteTable() {
        override val name: String = error("this should never be called")
    }

}
