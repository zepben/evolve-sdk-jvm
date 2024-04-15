/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.associations

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable

@Suppress("PropertyName")
class TableAssetOrganisationRolesAssets : SqliteTable() {

    val ASSET_ORGANISATION_ROLE_MRID: Column = Column(++columnIndex, "asset_organisation_role_mrid", "TEXT", NOT_NULL)
    val ASSET_MRID: Column = Column(++columnIndex, "asset_mrid", "TEXT", NOT_NULL)

    override val name = "asset_organisation_roles_assets"

    override val uniqueIndexColumns: MutableList<List<Column>> =
        super.uniqueIndexColumns.apply {
            add(listOf(ASSET_ORGANISATION_ROLE_MRID, ASSET_MRID))
        }

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(ASSET_ORGANISATION_ROLE_MRID))
            add(listOf(ASSET_MRID))
        }

}
