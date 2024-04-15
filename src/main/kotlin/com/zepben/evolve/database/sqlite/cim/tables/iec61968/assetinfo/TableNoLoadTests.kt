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
class TableNoLoadTests : TableTransformerTest() {

    val ENERGISED_END_VOLTAGE: Column = Column(++columnIndex, "energised_end_voltage", "INTEGER", NULL)
    val EXCITING_CURRENT: Column = Column(++columnIndex, "exciting_current", "NUMBER", NULL)
    val EXCITING_CURRENT_ZERO: Column = Column(++columnIndex, "exciting_current_zero", "NUMBER", NULL)
    val LOSS: Column = Column(++columnIndex, "loss", "INTEGER", NULL)
    val LOSS_ZERO: Column = Column(++columnIndex, "loss_zero", "INTEGER", NULL)

    override val name: String = "no_load_tests"

}
