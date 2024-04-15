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

@Suppress("PropertyName")
class TablePowerTransformerEnds : TableTransformerEnds() {

    val POWER_TRANSFORMER_MRID: Column = Column(++columnIndex, "power_transformer_mrid", "TEXT", NULL)
    val CONNECTION_KIND: Column = Column(++columnIndex, "connection_kind", "TEXT", NOT_NULL)
    val PHASE_ANGLE_CLOCK: Column = Column(++columnIndex, "phase_angle_clock", "INTEGER", NULL)
    val B: Column = Column(++columnIndex, "b", "NUMBER", NULL)
    val B0: Column = Column(++columnIndex, "b0", "NUMBER", NULL)
    val G: Column = Column(++columnIndex, "g", "NUMBER", NULL)
    val G0: Column = Column(++columnIndex, "g0", "NUMBER", NULL)
    val R: Column = Column(++columnIndex, "r", "NUMBER", NULL)
    val R0: Column = Column(++columnIndex, "r0", "NUMBER", NULL)
    val RATED_U: Column = Column(++columnIndex, "rated_u", "INTEGER", NULL)
    val X: Column = Column(++columnIndex, "x", "NUMBER", NULL)
    val X0: Column = Column(++columnIndex, "x0", "NUMBER", NULL)

    override val name: String = "power_transformer_ends"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(POWER_TRANSFORMER_MRID, END_NUMBER))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(POWER_TRANSFORMER_MRID))
        }

}
