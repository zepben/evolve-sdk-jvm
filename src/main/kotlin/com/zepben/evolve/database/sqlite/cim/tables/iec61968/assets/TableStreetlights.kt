/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableStreetlights : TableAssets() {

    val POLE_MRID: Column = Column(++columnIndex, "pole_mrid", "TEXT", NULL)
    val LAMP_KIND: Column = Column(++columnIndex, "lamp_kind", "TEXT", NOT_NULL)
    val LIGHT_RATING: Column = Column(++columnIndex, "light_rating", "INTEGER", NULL)

    override val name: String = "streetlights"

}
