/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite.tables

import com.zepben.annotations.EverythingIsNonnullByDefault
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NOT_NULL

@Suppress("PropertyName")
@EverythingIsNonnullByDefault
class TableVersion : SqliteTable() {

    val SUPPORTED_VERSION = 35

    val VERSION = Column(++columnIndex, "version", "TEXT", NOT_NULL)

    override fun name(): String {
        return "version"
    }

    override val tableClass = this.javaClass
    override val tableClassInstance = this

}
