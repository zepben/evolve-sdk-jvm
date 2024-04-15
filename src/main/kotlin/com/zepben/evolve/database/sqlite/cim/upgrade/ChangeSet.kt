/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade

import com.zepben.evolve.database.sqlite.common.TableVersion

/**
 * A base class for all change sets used to upgrade the schema in our databases.
 *
 * @property number The number of the change set. Must be an incrementing number matching the target version defined in [TableVersion].
 * @property commands The SQL commands to run for this change set.
 * @property preCommandHooks The hooks to execute before the commands for this change set.
 * @property postCommandHooks The hooks to execute after the commands for this change set.
 */
data class ChangeSet @JvmOverloads constructor(
    val number: Int,
    val commands: List<Change>,
    val preCommandHooks: List<Hook> = emptyList(),
    val postCommandHooks: List<Hook> = emptyList()
)
