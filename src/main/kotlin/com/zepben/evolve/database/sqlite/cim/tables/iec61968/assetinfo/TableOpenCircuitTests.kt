/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.assetinfo

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableOpenCircuitTests : TableTransformerTest() {

    val ENERGISED_END_STEP: Column = Column(++columnIndex, "energised_end_step", "INTEGER", NULL)
    val ENERGISED_END_VOLTAGE: Column = Column(++columnIndex, "energised_end_voltage", "INTEGER", NULL)
    val OPEN_END_STEP: Column = Column(++columnIndex, "open_end_step", "INTEGER", NULL)
    val OPEN_END_VOLTAGE: Column = Column(++columnIndex, "open_end_voltage", "INTEGER", NULL)
    val PHASE_SHIFT: Column = Column(++columnIndex, "phase_shift", "NUMBER", NULL)

    override val name: String = "open_circuit_tests"

}
