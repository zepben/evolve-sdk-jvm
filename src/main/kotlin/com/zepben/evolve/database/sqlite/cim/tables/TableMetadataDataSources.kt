/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables

import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL

@Suppress("PropertyName")
class TableMetadataDataSources : SqliteTable() {

    val SOURCE: Column = Column(++columnIndex, "source", "TEXT", NOT_NULL)
    val VERSION: Column = Column(++columnIndex, "version", "TEXT", NOT_NULL)
    val TIMESTAMP: Column = Column(++columnIndex, "timestamp", "TEXT", NOT_NULL)

    override val name: String = "metadata_data_sources"

}
