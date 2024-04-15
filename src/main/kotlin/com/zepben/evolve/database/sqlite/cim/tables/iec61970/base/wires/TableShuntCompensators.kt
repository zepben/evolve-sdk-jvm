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
abstract class TableShuntCompensators : TableRegulatingCondEq() {

    val SHUNT_COMPENSATOR_INFO_MRID: Column = Column(++columnIndex, "shunt_compensator_info_mrid", "TEXT", NULL)
    val GROUNDED: Column = Column(++columnIndex, "grounded", "BOOLEAN", NOT_NULL)
    val NOM_U: Column = Column(++columnIndex, "nom_u", "INTEGER", NULL)
    val PHASE_CONNECTION: Column = Column(++columnIndex, "phase_connection", "TEXT", NOT_NULL)
    val SECTIONS: Column = Column(++columnIndex, "sections", "NUMBER", NULL)

}
