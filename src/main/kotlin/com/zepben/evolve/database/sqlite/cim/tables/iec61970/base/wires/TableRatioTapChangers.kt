/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableRatioTapChangers : TableTapChangers() {

    val TRANSFORMER_END_MRID: Column = Column(++columnIndex, "transformer_end_mrid", "TEXT", NULL)
    val STEP_VOLTAGE_INCREMENT: Column = Column(++columnIndex, "step_voltage_increment", "NUMBER", NULL)

    override val name: String = "ratio_tap_changers"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(TRANSFORMER_END_MRID))
        }

}
