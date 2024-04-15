/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.auxiliaryequipment

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableCurrentTransformers : TableSensors() {

    val CURRENT_TRANSFORMER_INFO_MRID: Column = Column(++columnIndex, "current_transformer_info_mrid", "TEXT", NULL)
    val CORE_BURDEN: Column = Column(++columnIndex, "core_burden", "INTEGER", NULL)

    override val name: String = "current_transformers"

}
