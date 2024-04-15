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
class TableEnergySources : TableEnergyConnections() {

    val ACTIVE_POWER: Column = Column(++columnIndex, "active_power", "NUMBER", NULL)
    val REACTIVE_POWER: Column = Column(++columnIndex, "reactive_power", "NUMBER", NULL)
    val VOLTAGE_ANGLE: Column = Column(++columnIndex, "voltage_angle", "NUMBER", NULL)
    val VOLTAGE_MAGNITUDE: Column = Column(++columnIndex, "voltage_magnitude", "NUMBER", NULL)
    val P_MAX: Column = Column(++columnIndex, "p_max", "NUMBER", NULL)
    val P_MIN: Column = Column(++columnIndex, "p_min", "NUMBER", NULL)
    val R: Column = Column(++columnIndex, "r", "NUMBER", NULL)
    val R0: Column = Column(++columnIndex, "r0", "NUMBER", NULL)
    val RN: Column = Column(++columnIndex, "rn", "NUMBER", NULL)
    val X: Column = Column(++columnIndex, "x", "NUMBER", NULL)
    val X0: Column = Column(++columnIndex, "x0", "NUMBER", NULL)
    val XN: Column = Column(++columnIndex, "xn", "NUMBER", NULL)
    val IS_EXTERNAL_GRID: Column = Column(++columnIndex, "is_external_grid", "BOOLEAN", NOT_NULL)
    val R_MIN: Column = Column(++columnIndex, "r_min", "NUMBER", NULL)
    val RN_MIN: Column = Column(++columnIndex, "rn_min", "NUMBER", NULL)
    val R0_MIN: Column = Column(++columnIndex, "r0_min", "NUMBER", NULL)
    val X_MIN: Column = Column(++columnIndex, "x_min", "NUMBER", NULL)
    val XN_MIN: Column = Column(++columnIndex, "xn_min", "NUMBER", NULL)
    val X0_MIN: Column = Column(++columnIndex, "x0_min", "NUMBER", NULL)
    val R_MAX: Column = Column(++columnIndex, "r_max", "NUMBER", NULL)
    val RN_MAX: Column = Column(++columnIndex, "rn_max", "NUMBER", NULL)
    val R0_MAX: Column = Column(++columnIndex, "r0_max", "NUMBER", NULL)
    val X_MAX: Column = Column(++columnIndex, "x_max", "NUMBER", NULL)
    val XN_MAX: Column = Column(++columnIndex, "xn_max", "NUMBER", NULL)
    val X0_MAX: Column = Column(++columnIndex, "x0_max", "NUMBER", NULL)

    override val name: String = "energy_sources"

}
