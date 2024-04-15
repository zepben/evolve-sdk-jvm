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
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableConductingEquipment

@Suppress("PropertyName")
class TableSeriesCompensators : TableConductingEquipment() {

    val R: Column = Column(++columnIndex, "r", "NUMBER", NULL)
    val R0: Column = Column(++columnIndex, "r0", "NUMBER", NULL)
    val X: Column = Column(++columnIndex, "x", "NUMBER", NULL)
    val X0: Column = Column(++columnIndex, "x0", "NUMBER", NULL)
    val VARISTOR_RATED_CURRENT: Column = Column(++columnIndex, "varistor_rated_current", "INTEGER", NULL)
    val VARISTOR_VOLTAGE_THRESHOLD: Column = Column(++columnIndex, "varistor_voltage_threshold", "INTEGER", NULL)

    override val name: String = "series_compensators"

}
