/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.upgrade

import com.zepben.evolve.database.filepaths.PathType

/**
 * A class containing a list of SQL commands required to upgrade the schema, and which databases these changes should be run against.
 *
 * @property commands The SQL commands to run.
 * @property targetDatabases The database types that these commands should be run against.
 */
data class Change(
    val commands: List<String>,
    val targetDatabases: Set<PathType>,
)
