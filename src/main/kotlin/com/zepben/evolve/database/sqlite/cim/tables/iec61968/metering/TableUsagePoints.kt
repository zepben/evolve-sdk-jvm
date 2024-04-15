/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.metering

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableIdentifiedObjects

@Suppress("PropertyName")
class TableUsagePoints : TableIdentifiedObjects() {

    val LOCATION_MRID: Column = Column(++columnIndex, "location_mrid", "TEXT", NULL)
    val IS_VIRTUAL: Column = Column(++columnIndex, "is_virtual", "BOOLEAN")
    val CONNECTION_CATEGORY: Column = Column(++columnIndex, "connection_category", "TEXT", NULL)
    val RATED_POWER: Column = Column(++columnIndex, "rated_power", "INTEGER", NULL)
    val APPROVED_INVERTER_CAPACITY: Column = Column(++columnIndex, "approved_inverter_capacity", "INTEGER", NULL)

    override val name: String = "usage_points"

}
