/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common.metadata

import com.zepben.evolve.database.sqlite.common.BaseCollectionReader
import com.zepben.evolve.database.sqlite.common.BaseDatabaseTables
import com.zepben.evolve.database.sqlite.tables.TableMetadataDataSources
import com.zepben.evolve.services.common.meta.MetadataCollection
import java.sql.Connection

/**
 * Class for reading the [MetadataCollection] from the database.
 *
 * @param metadata The [MetadataCollection] to populate from the database.
 * @param databaseTables The tables available in the database.
 * @param connection The [Connection] to the database.
 */
class MetadataCollectionReader @JvmOverloads constructor(
    metadata: MetadataCollection,
    databaseTables: BaseDatabaseTables,
    connection: Connection,
    private val reader: MetadataEntryReader = MetadataEntryReader(metadata),
) : BaseCollectionReader(databaseTables, connection) {

    override fun load(): Boolean =
        loadEach<TableMetadataDataSources>(reader::load)

}
