/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.protection

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TableCurrentRelays : TableProtectionRelayFunctions() {

    val CURRENT_LIMIT_1: Column = Column(++columnIndex, "current_limit_1", "NUMBER", NULL)
    val INVERSE_TIME_FLAG: Column = Column(++columnIndex, "inverse_time_flag", "BOOLEAN", NULL)
    val TIME_DELAY_1: Column = Column(++columnIndex, "time_delay_1", "NUMBER", NULL)

    override val name: String = "current_relays"

}
