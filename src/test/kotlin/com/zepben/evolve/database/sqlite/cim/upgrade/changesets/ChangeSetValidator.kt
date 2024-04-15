/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import java.sql.ResultSet
import java.sql.Statement


interface ChangeSetValidator {

    /**
     * Set up prior to applying the ChangeSet.
     * @return A list of statements to run on the database to which the ChangeSet will be applied.
     */
    fun setUpStatements(): List<String>

    /**
     * Populate any tables or fields with data after applying the ChangeSet.
     * @return A list of statements to run on the database to which the ChangeSet has been applied.
     */
    fun populateStatements(): List<String>

    /**
     * Validation after applying the ChangeSet
     * @param statement A statement that can be used to run queries against the database to which the ChangeSet has been applied.
     */
    fun validate(statement: Statement)

    /**
     * Tear down after validating the ChangeSet.
     * @return A list of statements to remove any data from the database to which the ChangeSet was applied.
     */
    fun tearDownStatements(): List<String>

    /**
     * Validate each row returned from a query.
     * @param statement The statement used to run the query.
     * @param sql The query string.
     * @param validators A list of validators, one per expected row in the query.
     */
    fun validateRows(statement: Statement, sql: String, vararg validators: (ResultSet) -> Unit) {
        statement.executeQuery(sql).use { rs ->
            validators.forEach {
                assertThat(rs.next(), equalTo(true))
                it(rs)
            }
            assertThat(rs.next(), equalTo(false))
        }
    }

    fun ensureIndexes(statement: Statement, vararg expectedIndexes: String, present: Boolean = true) {
        expectedIndexes.forEach {
            statement.executeQuery("pragma index_info('$it')").use { rs ->
                assertThat("Does index '$it' exist", rs.next(), equalTo(present))
            }
        }
    }

    fun ensureTables(statement: Statement, vararg tableNames: String, present: Boolean = true) {
        tableNames.forEach {
            statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$it';").use { rs ->
                assertThat("Does table '$it' exist", rs.next(), equalTo(present))
            }
            if (present) {
                statement.executeQuery("SELECT count(*) FROM $it;").use { rs ->
                    rs.next()
                    assertThat("Table '$it' should still be populated", rs.getInt(1), not(equalTo(0)))
                }
            }
        }
    }

    fun ensureColumns(statement: Statement, table: String, vararg expectedColumns: String, present: Boolean = true) {
        statement.executeQuery("pragma table_info('$table')").use { rs ->
            val columns = mutableListOf<String>()
            while (rs.next()) {
                columns.add(rs.getString("name"))
            }
            expectedColumns.forEach {
                assertThat("Does column '$table.$it' exist", it in columns, equalTo(present))
            }
        }
    }

}
