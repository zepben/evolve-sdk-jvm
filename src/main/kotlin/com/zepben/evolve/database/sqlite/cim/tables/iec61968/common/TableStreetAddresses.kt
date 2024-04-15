/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.common

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
abstract class TableStreetAddresses : TableTownDetails() {

    val POSTAL_CODE: Column = Column(++columnIndex, "postal_code", "TEXT", NOT_NULL)
    val PO_BOX: Column = Column(++columnIndex, "po_box", "TEXT", NULL)
    val BUILDING_NAME: Column = Column(++columnIndex, "building_name", "TEXT", NULL)
    val FLOOR_IDENTIFICATION: Column = Column(++columnIndex, "floor_identification", "TEXT", NULL)
    val STREET_NAME: Column = Column(++columnIndex, "name", "TEXT", NULL)
    val NUMBER: Column = Column(++columnIndex, "number", "TEXT", NULL)
    val SUITE_NUMBER: Column = Column(++columnIndex, "suite_number", "TEXT", NULL)
    val TYPE: Column = Column(++columnIndex, "type", "TEXT", NULL)
    val DISPLAY_ADDRESS: Column = Column(++columnIndex, "display_address", "TEXT", NULL)

}
