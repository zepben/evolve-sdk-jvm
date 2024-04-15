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

@Suppress("PropertyName")
class TableAcLineSegments : TableConductors() {

    val PER_LENGTH_SEQUENCE_IMPEDANCE_MRID: Column =
        Column(++columnIndex, "per_length_sequence_impedance_mrid", "TEXT", NULL)

    override val name: String = "ac_line_segments"

}
