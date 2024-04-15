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
class TableEnergyConsumerPhases : TablePowerSystemResources() {

    val ENERGY_CONSUMER_MRID: Column = Column(++columnIndex, "energy_consumer_mrid", "TEXT", NOT_NULL)
    val PHASE: Column = Column(++columnIndex, "phase", "TEXT", NOT_NULL)
    val P: Column = Column(++columnIndex, "p", "NUMBER", NULL)
    val Q: Column = Column(++columnIndex, "q", "NUMBER", NULL)
    val P_FIXED: Column = Column(++columnIndex, "p_fixed", "NUMBER", NULL)
    val Q_FIXED: Column = Column(++columnIndex, "q_fixed", "NUMBER", NULL)

    override val name: String = "energy_consumer_phases"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(ENERGY_CONSUMER_MRID, PHASE))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(ENERGY_CONSUMER_MRID))
        }

}
