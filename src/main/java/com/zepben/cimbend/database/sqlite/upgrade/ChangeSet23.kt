/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.cimbend.database.sqlite.upgrade

internal fun changeSet23() = ChangeSet(23) {
    listOf(
        "ALTER TABLE power_transformers ADD transformer_utilisation NUMBER NOT NULL DEFAULT 0"
    )
}
