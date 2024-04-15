/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets

import com.zepben.evolve.database.sqlite.cim.upgrade.Change
import com.zepben.evolve.database.sqlite.cim.upgrade.ChangeSet
import com.zepben.evolve.database.paths.DatabaseType

internal fun changeSet47() = ChangeSet(
    47,
    listOf(
        `Delete all reclose sequences`
    )
)

@Suppress("ObjectPropertyName")
private val `Delete all reclose sequences` = Change(
    listOf(
        "DROP INDEX IF EXISTS reclose_sequences_mrid;",
        "DROP INDEX IF EXISTS reclose_sequences_name;",
        "DROP TABLE IF EXISTS reclose_sequences;",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)
