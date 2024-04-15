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
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableConductingEquipment

@Suppress("PropertyName")
class TablePowerTransformers : TableConductingEquipment() {

    val VECTOR_GROUP: Column = Column(++columnIndex, "vector_group", "TEXT", NOT_NULL)
    val TRANSFORMER_UTILISATION: Column = Column(++columnIndex, "transformer_utilisation", "NUMBER", NULL)
    val CONSTRUCTION_KIND: Column = Column(++columnIndex, "construction_kind", "TEXT", NOT_NULL)
    val FUNCTION: Column = Column(++columnIndex, "function", "TEXT", NOT_NULL)
    val POWER_TRANSFORMER_INFO_MRID: Column = Column(++columnIndex, "power_transformer_info_mrid", "TEXT", NULL)

    override val name: String = "power_transformers"

}
