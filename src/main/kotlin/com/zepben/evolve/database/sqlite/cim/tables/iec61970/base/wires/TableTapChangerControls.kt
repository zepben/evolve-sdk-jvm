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
class TableTapChangerControls : TableRegulatingControls() {

    val LIMIT_VOLTAGE: Column = Column(++columnIndex, "limit_voltage", "INTEGER", NULL)
    val LINE_DROP_COMPENSATION: Column = Column(++columnIndex, "line_drop_compensation", "BOOLEAN", NULL)
    val LINE_DROP_R: Column = Column(++columnIndex, "line_drop_r", "NUMBER", NULL)
    val LINE_DROP_X: Column = Column(++columnIndex, "line_drop_x", "NUMBER", NULL)
    val REVERSE_LINE_DROP_R: Column = Column(++columnIndex, "reverse_line_drop_r", "NUMBER", NULL)
    val REVERSE_LINE_DROP_X: Column = Column(++columnIndex, "reverse_line_drop_x", "NUMBER", NULL)
    val FORWARD_LDC_BLOCKING: Column = Column(++columnIndex, "forward_ldc_blocking", "BOOLEAN", NULL)
    val TIME_DELAY: Column = Column(++columnIndex, "time_delay", "NUMBER", NULL)
    val CO_GENERATION_ENABLED: Column = Column(++columnIndex, "co_generation_enabled", "BOOLEAN", NULL)

    override val name: String = "tap_changer_controls"

}
