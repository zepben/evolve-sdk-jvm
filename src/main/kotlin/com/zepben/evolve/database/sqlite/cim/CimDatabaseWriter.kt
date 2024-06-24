/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim

import com.zepben.evolve.database.sqlite.cim.metadata.MetadataCollectionWriter
import com.zepben.evolve.database.sqlite.common.BaseDatabaseWriter
import java.sql.Connection

/**
 * A base class for writing objects to one of our CIM databases.
 *
 * @param databaseFile The filename of the database to write.
 * @param databaseTables The tables to create in the database.
 * @param getConnection Provider of the connection to the specified database.
 * @param metadataWriter The [MetadataCollectionWriter] to use.
 * @param serviceWriter The [BaseServiceWriter] to use.
 *
 * @property logger The logger to use for this database writer.
 */
abstract class CimDatabaseWriter(
    databaseFile: String,
    databaseTables: CimDatabaseTables,
    getConnection: (String) -> Connection,
    private val metadataWriter: MetadataCollectionWriter,
    private val serviceWriter: BaseServiceWriter
) : BaseDatabaseWriter(databaseFile, databaseTables, getConnection) {

    /**
     * Save metadata and service.
     */
    override fun saveSchema(): Boolean = metadataWriter.save() and serviceWriter.save()

}
