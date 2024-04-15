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
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets.TableAssetInfo

@Suppress("PropertyName")
class TableShuntCompensatorInfo : TableAssetInfo() {

    val MAX_POWER_LOSS: Column = Column(++columnIndex, "max_power_loss", "INTEGER", NULL)
    val RATED_CURRENT: Column = Column(++columnIndex, "rated_current", "INTEGER", NULL)
    val RATED_REACTIVE_POWER: Column = Column(++columnIndex, "rated_reactive_power", "INTEGER", NULL)
    val RATED_VOLTAGE: Column = Column(++columnIndex, "rated_voltage", "INTEGER", NULL)

    override val name: String = "shunt_compensator_info"

}
