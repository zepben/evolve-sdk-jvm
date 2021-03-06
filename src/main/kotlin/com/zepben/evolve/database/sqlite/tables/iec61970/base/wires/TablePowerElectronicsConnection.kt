/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite.tables.iec61970.base.wires

import com.zepben.evolve.database.sqlite.tables.Column
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TablePowerElectronicsConnection : TableRegulatingCondEq() {

    val MAX_I_FAULT = Column(++columnIndex, "max_i_fault", "NUMBER", NULL)
    val MAX_Q = Column(++columnIndex, "max_q", "NUMBER", NULL)
    val MIN_Q = Column(++columnIndex, "min_q", "NUMBER", NULL)
    val P = Column(++columnIndex, "p", "NUMBER", NULL)
    val Q = Column(++columnIndex, "q", "NUMBER", NULL)
    val RATED_S = Column(++columnIndex, "rated_s", "NUMBER", NULL)
    val RATED_U = Column(++columnIndex, "rated_u", "NUMBER", NULL)

    override fun name(): String {
        return "power_electronics_connection"
    }

    override val tableClass = this.javaClass
    override val tableClassInstance = this

}
