/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
abstract class TableEquipment : TablePowerSystemResources() {

    val NORMALLY_IN_SERVICE: Column = Column(++columnIndex, "normally_in_service", "BOOLEAN")
    val IN_SERVICE: Column = Column(++columnIndex, "in_service", "BOOLEAN")
    val COMMISSIONED_DATE: Column = Column(++columnIndex, "commissioned_date", "TEXT", NULL)

}
