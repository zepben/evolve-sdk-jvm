/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.customer

import com.zepben.evolve.database.sqlite.cim.CimDatabaseReader
import com.zepben.evolve.database.sqlite.cim.metadata.MetadataCollectionReader
import com.zepben.evolve.database.sqlite.cim.tables.tableCimVersion
import com.zepben.evolve.database.sqlite.common.TableVersion
import com.zepben.evolve.services.common.meta.MetadataCollection
import com.zepben.evolve.services.customer.CustomerService
import java.sql.Connection

/**
 * A class for reading the [CustomerService] objects and [MetadataCollection] from our customer database.
 *
 * @param connection The connection to the database.
 * @param metadata The [MetadataCollection] to populate with metadata from the database.
 * @param service The [CustomerService] to populate with CIM objects from the database.
 * @param databaseDescription The description of the database for logging (e.g. filename).
 */
class CustomerDatabaseReader @JvmOverloads constructor(
    connection: Connection,
    metadata: MetadataCollection,
    override val service: CustomerService,
    databaseDescription: String,
    tables: CustomerDatabaseTables = CustomerDatabaseTables(),
    metadataReader: MetadataCollectionReader = MetadataCollectionReader(metadata, tables, connection),
    serviceReader: CustomerServiceReader = CustomerServiceReader(service, tables, connection),
    tableVersion: TableVersion = tableCimVersion
) : CimDatabaseReader(connection, metadataReader, serviceReader, service, databaseDescription, tableVersion)
