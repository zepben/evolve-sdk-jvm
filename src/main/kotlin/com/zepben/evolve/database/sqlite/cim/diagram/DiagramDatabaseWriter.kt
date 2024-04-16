/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.diagram

import com.zepben.evolve.database.sqlite.cim.CimDatabaseWriter
import com.zepben.evolve.database.sqlite.cim.metadata.MetadataCollectionWriter
import com.zepben.evolve.services.common.meta.MetadataCollection
import com.zepben.evolve.services.diagram.DiagramService
import java.sql.Connection
import java.sql.DriverManager

/**
 * A class for writing the [DiagramService] objects and [MetadataCollection] to our diagram database.
 *
 * @param databaseFile the filename of the database to write.
 * @param metadata The [MetadataCollection] to save to the database.
 * @param service The [DiagramService] to save to the database.
 */
class DiagramDatabaseWriter @JvmOverloads constructor(
    databaseFile: String,
    metadata: MetadataCollection,
    service: DiagramService,
    databaseTables: DiagramDatabaseTables = DiagramDatabaseTables(),
    createMetadataWriter: (Connection) -> MetadataCollectionWriter = { MetadataCollectionWriter(metadata, databaseTables) },
    createServiceWriter: (Connection) -> DiagramServiceWriter = { DiagramServiceWriter(service, databaseTables) },
    getConnection: (String) -> Connection = DriverManager::getConnection
) : CimDatabaseWriter(
    databaseFile,
    databaseTables,
    getConnection,
    createMetadataWriter,
    createServiceWriter
)
