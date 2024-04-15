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
class TablePerLengthSequenceImpedances : com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires.TablePerLengthImpedances() {

    val R: Column = Column(++columnIndex, "r", "NUMBER", NULL)
    val X: Column = Column(++columnIndex, "x", "NUMBER", NULL)
    val R0: Column = Column(++columnIndex, "r0", "NUMBER", NULL)
    val X0: Column = Column(++columnIndex, "x0", "NUMBER", NULL)
    val BCH: Column = Column(++columnIndex, "bch", "NUMBER", NULL)
    val GCH: Column = Column(++columnIndex, "gch", "NUMBER", NULL)
    val B0CH: Column = Column(++columnIndex, "b0ch", "NUMBER", NULL)
    val G0CH: Column = Column(++columnIndex, "g0ch", "NUMBER", NULL)

    override val name: String = "per_length_sequence_impedances"

}
