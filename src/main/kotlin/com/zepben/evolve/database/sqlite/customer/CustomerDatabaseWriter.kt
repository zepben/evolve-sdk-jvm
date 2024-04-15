/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.customer

import com.zepben.evolve.database.sqlite.common.cim.CimDatabaseWriter
import com.zepben.evolve.database.sqlite.common.metadata.MetadataCollectionWriter
import com.zepben.evolve.services.common.meta.MetadataCollection
import com.zepben.evolve.services.customer.CustomerService
import java.sql.Connection
import java.sql.DriverManager

/**
 * A class for writing the [CustomerService] objects and [MetadataCollection] to our customer database.
 *
 * @param databaseFile the filename of the database to write.
 * @param metadata The [MetadataCollection] to save to the database.
 * @param service The [CustomerService] to save to the database.
 */
class CustomerDatabaseWriter @JvmOverloads constructor(
    databaseFile: String,
    metadata: MetadataCollection,
    service: CustomerService,
    databaseTables: CustomerDatabaseTables = CustomerDatabaseTables(),
    createMetadataWriter: (Connection) -> MetadataCollectionWriter = { MetadataCollectionWriter(metadata, databaseTables) },
    createServiceWriter: (Connection) -> CustomerServiceWriter = { CustomerServiceWriter(service, databaseTables) },
    getConnection: (String) -> Connection = DriverManager::getConnection
) : CimDatabaseWriter(
    databaseFile,
    databaseTables,
    createMetadataWriter,
    createServiceWriter,
    getConnection
)
