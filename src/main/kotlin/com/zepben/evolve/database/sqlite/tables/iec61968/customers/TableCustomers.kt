/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.tables.iec61968.customers

import com.zepben.evolve.database.sqlite.tables.Column
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TableOrganisationRoles

@Suppress("PropertyName")
class TableCustomers : TableOrganisationRoles() {

    val KIND: Column = Column(++columnIndex, "kind", "TEXT", NOT_NULL)
    val NUM_END_DEVICES: Column = Column(++columnIndex, "num_end_devices", "INTEGER", NULL)

    override fun name(): String {
        return "customers"
    }

    override val tableClass: Class<TableCustomers> = this.javaClass
    override val tableClassInstance: TableCustomers = this

}
