/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets.combined

import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.ChangeSetValidator
import java.sql.Statement

object ChangeSet47Validator : ChangeSetValidator {
    override fun setUpStatements(): List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS reclose_sequences (mrid TEXT NOT NULL, name TEXT NOT NULL);",
        "CREATE UNIQUE INDEX reclose_sequences_mrid ON reclose_sequences (mrid);",
        "CREATE INDEX reclose_sequences_name ON reclose_sequences (name);",

        )

    override fun populateStatements(): List<String> = emptyList()

    override fun validate(statement: Statement) {
        ensureTables(statement, "reclose_sequences", present = false)
        ensureIndexes(statement, "reclose_sequences_mrid", "reclose_sequences_name", present = false)
    }

    override fun tearDownStatements(): List<String> = emptyList()

}
