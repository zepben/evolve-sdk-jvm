/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.diagram

import com.zepben.evolve.database.sqlite.common.cim.CimDatabaseReader
import com.zepben.evolve.database.sqlite.common.metadata.MetadataCollectionReader
import com.zepben.evolve.database.sqlite.tables.TableVersion
import com.zepben.evolve.services.common.meta.MetadataCollection
import com.zepben.evolve.services.diagram.DiagramService
import java.sql.Connection

/**
 * A class for reading the [DiagramService] objects and [MetadataCollection] from our diagram database.
 *
 * @param connection The connection to the database.
 * @param metadata The [MetadataCollection] to populate with metadata from the database.
 * @param service The [DiagramService] to populate with CIM objects from the database.
 * @param databaseDescription The description of the database for logging (e.g. filename).
 */
class DiagramDatabaseReader @JvmOverloads constructor(
    connection: Connection,
    metadata: MetadataCollection,
    override val service: DiagramService,
    databaseDescription: String,
    tables: DiagramDatabaseTables = DiagramDatabaseTables(),
    metadataReader: MetadataCollectionReader = MetadataCollectionReader(metadata, tables, connection),
    serviceReader: DiagramServiceReader = DiagramServiceReader(service, tables, connection),
    tableVersion: TableVersion = TableVersion()
) : CimDatabaseReader(connection, metadataReader, serviceReader, service, databaseDescription, tableVersion)
