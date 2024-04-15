/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets

import java.sql.Statement

/**
 * Helper class for changeset that do not impact a database.
 *
 * NOTE: This doesn't actually check that no changes were made, it is just a convenient way to create a changeset with no expected changes.
 */
open class NoChanges : ChangeSetValidator {

    override fun setUpStatements(): List<String> = emptyList()
    override fun populateStatements(): List<String> = emptyList()
    override fun validate(statement: Statement) {}
    override fun tearDownStatements(): List<String> = emptyList()

}
