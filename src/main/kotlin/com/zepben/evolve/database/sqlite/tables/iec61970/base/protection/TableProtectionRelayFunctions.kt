/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.tables.iec61970.base.protection

import com.zepben.evolve.database.sqlite.tables.Column
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.TablePowerSystemResources

@Suppress("PropertyName")
abstract class TableProtectionRelayFunctions : TablePowerSystemResources() {

    val MODEL = Column(++columnIndex, "model", "TEXT", NULL)
    val RECLOSING = Column(++columnIndex, "reclosing", "BOOLEAN", NULL)
    val RELAY_DELAY_TIME = Column(++columnIndex, "relay_delay_time", "NUMBER", NULL)
    val PROTECTION_KIND = Column(++columnIndex, "protection_kind", "TEXT", NOT_NULL)
    val DIRECTABLE = Column(++columnIndex, "directable", "BOOLEAN", NULL)
    val POWER_DIRECTION = Column(++columnIndex, "power_direction", "TEXT", NOT_NULL)
    val RELAY_INFO_MRID = Column(++columnIndex, "relay_info_mrid", "TEXT", NULL)

}
