/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.common

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableIdentifiedObjects

@Suppress("PropertyName")
abstract class TableDocuments : TableIdentifiedObjects() {

    val TITLE: Column = Column(++columnIndex, "title", "TEXT", NOT_NULL)
    val CREATED_DATE_TIME: Column = Column(++columnIndex, "created_date_time", "TEXT", NULL)
    val AUTHOR_NAME: Column = Column(++columnIndex, "author_name", "TEXT", NOT_NULL)
    val TYPE: Column = Column(++columnIndex, "type", "TEXT", NOT_NULL)
    val STATUS: Column = Column(++columnIndex, "status", "TEXT", NOT_NULL)
    val COMMENT: Column = Column(++columnIndex, "comment", "TEXT", NOT_NULL)

}
