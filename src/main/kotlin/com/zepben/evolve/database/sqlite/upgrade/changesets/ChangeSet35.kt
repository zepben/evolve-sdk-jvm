/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite.upgrade.changesets

import com.zepben.evolve.database.sqlite.upgrade.ChangeSet

internal fun changeSet35() = ChangeSet(35) {
    listOf(
        *`Add usage point fields`,
    )
}

@Suppress("ObjectPropertyName")
private val `Add usage point fields` = arrayOf(
    "ALTER TABLE usage_points ADD is_virtual BOOLEAN",
    "ALTER TABLE usage_points ADD connection_category TEXT NULL"
)
