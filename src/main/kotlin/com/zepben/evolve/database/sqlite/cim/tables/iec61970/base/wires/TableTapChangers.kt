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
abstract class TableTapChangers : TablePowerSystemResources() {

    val CONTROL_ENABLED: Column = Column(++columnIndex, "control_enabled", "BOOLEAN", NOT_NULL)
    val HIGH_STEP: Column = Column(++columnIndex, "high_step", "INTEGER", NULL)
    val LOW_STEP: Column = Column(++columnIndex, "low_step", "INTEGER", NULL)
    val NEUTRAL_STEP: Column = Column(++columnIndex, "neutral_step", "INTEGER", NULL)
    val NEUTRAL_U: Column = Column(++columnIndex, "neutral_u", "INTEGER", NULL)
    val NORMAL_STEP: Column = Column(++columnIndex, "normal_step", "INTEGER", NULL)
    val STEP: Column = Column(++columnIndex, "step", "NUMBER", NULL)
    val TAP_CHANGER_CONTROL_MRID: Column = Column(++columnIndex, "tap_changer_control_mrid", "TEXT", NULL)

}
