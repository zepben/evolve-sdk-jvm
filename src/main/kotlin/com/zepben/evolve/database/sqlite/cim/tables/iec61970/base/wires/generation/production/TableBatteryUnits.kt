/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires.generation.production

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableBatteryUnits : TablePowerElectronicsUnits() {

    val BATTERY_STATE: Column = Column(++columnIndex, "battery_state", "TEXT", NOT_NULL)
    val RATED_E: Column = Column(++columnIndex, "rated_e", "INTEGER", NULL)
    val STORED_E: Column = Column(++columnIndex, "stored_e", "INTEGER", NULL)

    override val name: String = "battery_units"

}
