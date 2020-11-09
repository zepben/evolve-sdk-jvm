/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.cimbend.database.sqlite.tables.iec61968.assets

class TableAssetOwners : TableAssetOrganisationRoles() {

    override fun name(): String {
        return "asset_owners"
    }

    override val tableClass = this.javaClass
    override val tableClassInstance = this

}
