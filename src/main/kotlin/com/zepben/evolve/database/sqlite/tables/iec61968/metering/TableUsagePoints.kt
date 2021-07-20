/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite.tables.iec61968.metering

import com.zepben.evolve.database.sqlite.tables.Column
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.TableIdentifiedObjects

@Suppress("PropertyName")
class TableUsagePoints : TableIdentifiedObjects() {

    val LOCATION_MRID = Column(++columnIndex, "location_mrid", "TEXT", NULL)
    val IS_VIRTUAL = Column(++columnIndex, "is_virtual", "BOOLEAN")
    val CONNECTION_CATEGORY = Column(++columnIndex, "connection_category", "TEXT", NULL)

    override fun name(): String {
        return "usage_points"
    }

    override val tableClass = this.javaClass
    override val tableClassInstance = this

}
