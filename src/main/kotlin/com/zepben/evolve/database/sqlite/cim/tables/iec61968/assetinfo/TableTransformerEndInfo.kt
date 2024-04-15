/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.assetinfo

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets.TableAssetInfo

@Suppress("PropertyName")
class TableTransformerEndInfo : TableAssetInfo() {

    val CONNECTION_KIND: Column = Column(++columnIndex, "connection_kind", "TEXT", NOT_NULL)
    val EMERGENCY_S: Column = Column(++columnIndex, "emergency_s", "INTEGER", NULL)
    val END_NUMBER: Column = Column(++columnIndex, "end_number", "INTEGER", NOT_NULL)
    val INSULATION_U: Column = Column(++columnIndex, "insulation_u", "INTEGER", NULL)
    val PHASE_ANGLE_CLOCK: Column = Column(++columnIndex, "phase_angle_clock", "INTEGER", NULL)
    val R: Column = Column(++columnIndex, "r", "NUMBER", NULL)
    val RATED_S: Column = Column(++columnIndex, "rated_s", "INTEGER", NULL)
    val RATED_U: Column = Column(++columnIndex, "rated_u", "INTEGER", NULL)
    val SHORT_TERM_S: Column = Column(++columnIndex, "short_term_s", "INTEGER", NULL)
    val TRANSFORMER_TANK_INFO_MRID: Column = Column(++columnIndex, "transformer_tank_info_mrid", "TEXT", NULL)
    val ENERGISED_END_NO_LOAD_TESTS: Column = Column(++columnIndex, "energised_end_no_load_tests", "TEXT", NULL)
    val ENERGISED_END_SHORT_CIRCUIT_TESTS: Column = Column(++columnIndex, "energised_end_short_circuit_tests", "TEXT", NULL)
    val GROUNDED_END_SHORT_CIRCUIT_TESTS: Column = Column(++columnIndex, "grounded_end_short_circuit_tests", "TEXT", NULL)
    val OPEN_END_OPEN_CIRCUIT_TESTS: Column = Column(++columnIndex, "open_end_open_circuit_tests", "TEXT", NULL)
    val ENERGISED_END_OPEN_CIRCUIT_TESTS: Column = Column(++columnIndex, "energised_end_open_circuit_tests", "TEXT", NULL)

    override val name: String = "transformer_end_info"

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(TRANSFORMER_TANK_INFO_MRID))
            add(listOf(ENERGISED_END_NO_LOAD_TESTS))
            add(listOf(ENERGISED_END_SHORT_CIRCUIT_TESTS))
            add(listOf(GROUNDED_END_SHORT_CIRCUIT_TESTS))
            add(listOf(OPEN_END_OPEN_CIRCUIT_TESTS))
            add(listOf(ENERGISED_END_OPEN_CIRCUIT_TESTS))
        }

}
