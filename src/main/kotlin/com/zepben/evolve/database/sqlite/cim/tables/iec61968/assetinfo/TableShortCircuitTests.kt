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
class TableShortCircuitTests : TableTransformerTest() {

    val CURRENT: Column = Column(++columnIndex, "current", "NUMBER", NULL)
    val ENERGISED_END_STEP: Column = Column(++columnIndex, "energised_end_step", "INTEGER", NULL)
    val GROUNDED_END_STEP: Column = Column(++columnIndex, "grounded_end_step", "INTEGER", NULL)
    val LEAKAGE_IMPEDANCE: Column = Column(++columnIndex, "leakage_impedance", "NUMBER", NULL)
    val LEAKAGE_IMPEDANCE_ZERO: Column = Column(++columnIndex, "leakage_impedance_zero", "NUMBER", NULL)
    val LOSS: Column = Column(++columnIndex, "loss", "INTEGER", NULL)
    val LOSS_ZERO: Column = Column(++columnIndex, "loss_zero", "INTEGER", NULL)
    val POWER: Column = Column(++columnIndex, "power", "INTEGER", NULL)
    val VOLTAGE: Column = Column(++columnIndex, "voltage", "NUMBER", NULL)
    val VOLTAGE_OHMIC_PART: Column = Column(++columnIndex, "voltage_ohmic_part", "NUMBER", NULL)

    override val name: String = "short_circuit_tests"

}
