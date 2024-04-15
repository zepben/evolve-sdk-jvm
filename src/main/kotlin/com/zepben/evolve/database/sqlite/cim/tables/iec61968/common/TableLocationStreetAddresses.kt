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

@Suppress("PropertyName")
class TableLocationStreetAddresses : TableStreetAddresses() {

    val LOCATION_MRID: Column = Column(++columnIndex, "location_mrid", "TEXT", NOT_NULL)
    val ADDRESS_FIELD: Column = Column(++columnIndex, "address_field", "TEXT", NOT_NULL)

    override val name: String = "location_street_addresses"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(LOCATION_MRID, ADDRESS_FIELD))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(LOCATION_MRID))
        }

}
