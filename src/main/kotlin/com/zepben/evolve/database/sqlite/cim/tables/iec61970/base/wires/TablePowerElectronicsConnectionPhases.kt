/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TablePowerSystemResources

@Suppress("PropertyName")
class TablePowerElectronicsConnectionPhases : TablePowerSystemResources() {

    val POWER_ELECTRONICS_CONNECTION_MRID: Column = Column(++columnIndex, "power_electronics_connection_mrid", "TEXT", NULL)
    val P: Column = Column(++columnIndex, "p", "NUMBER", NULL)
    val PHASE: Column = Column(++columnIndex, "phase", "TEXT", NOT_NULL)
    val Q: Column = Column(++columnIndex, "q", "NUMBER", NULL)

    override val name: String = "power_electronics_connection_phases"

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(POWER_ELECTRONICS_CONNECTION_MRID))
        }

}
