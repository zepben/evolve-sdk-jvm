/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade

import com.zepben.evolve.database.paths.DatabaseType
import java.sql.Statement

/**
 * A class containing hooks required to upgrade the schema, and which databases these changes should be run against. Hooks are sometimes needed when
 * the upgrade has more complex needs than what simple SQL statements can achieve.
 *
 * @property targetDatabases The database types that these hooks should be executed against.
 * @property action The execution block of the hook.
 */
data class Hook @JvmOverloads constructor(
    val targetDatabases: Set<DatabaseType> = setOf(DatabaseType.NETWORK_MODEL),
    val action: (Statement) -> Unit = { },
) {

    /**
     * Invoke the execution block for hook
     */
    operator fun invoke(statement: Statement): Unit = action(statement)

}
