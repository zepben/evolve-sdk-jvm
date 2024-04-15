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
class TableEnergyConsumers : TableEnergyConnections() {

    val CUSTOMER_COUNT: Column = Column(++columnIndex, "customer_count", "INTEGER", NULL)
    val GROUNDED: Column = Column(++columnIndex, "grounded", "BOOLEAN", NOT_NULL)
    val P: Column = Column(++columnIndex, "p", "NUMBER", NULL)
    val Q: Column = Column(++columnIndex, "q", "NUMBER", NULL)
    val P_FIXED: Column = Column(++columnIndex, "p_fixed", "NUMBER", NULL)
    val Q_FIXED: Column = Column(++columnIndex, "q_fixed", "NUMBER", NULL)
    val PHASE_CONNECTION: Column = Column(++columnIndex, "phase_connection", "TEXT", NOT_NULL)

    override val name: String = "energy_consumers"

}
