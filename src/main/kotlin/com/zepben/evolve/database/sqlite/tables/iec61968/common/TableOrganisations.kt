/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite.tables.iec61968.common

import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.TableIdentifiedObjects

class TableOrganisations : TableIdentifiedObjects() {

    override fun name(): String {
        return "organisations"
    }

    override val tableClass: Class<TableOrganisations> = this.javaClass
    override val tableClassInstance: TableOrganisations = this

}
