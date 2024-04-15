/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.customer

import com.zepben.evolve.database.sqlite.common.BaseServiceReader
import com.zepben.evolve.database.sqlite.cim.tables.associations.TableCustomerAgreementsPricingStructures
import com.zepben.evolve.database.sqlite.cim.tables.associations.TablePricingStructuresTariffs
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TableOrganisations
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.customers.TableCustomerAgreements
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.customers.TableCustomers
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.customers.TablePricingStructures
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.customers.TableTariffs
import com.zepben.evolve.services.customer.CustomerService
import java.sql.Connection

/**
 * A class for reading a [CustomerService] from the database.
 *
 * @param service The [CustomerService] to populate from the database.
 * @param databaseTables The tables available in the database.
 * @param connection A connection to the database.
 *
 * @property reader The [CustomerCimReader] used to load the objects from the database.
 */
class CustomerServiceReader @JvmOverloads constructor(
    service: CustomerService,
    databaseTables: CustomerDatabaseTables,
    connection: Connection,
    override val reader: CustomerCimReader = CustomerCimReader(service)
) : BaseServiceReader(databaseTables, connection, reader) {

    override fun doLoad(): Boolean =
        loadEach<TableOrganisations>(reader::load)
            .andLoadEach<TableCustomers>(reader::load)
            .andLoadEach<TableCustomerAgreements>(reader::load)
            .andLoadEach<TablePricingStructures>(reader::load)
            .andLoadEach<TableTariffs>(reader::load)
            .andLoadEach<TableCustomerAgreementsPricingStructures>(reader::load)
            .andLoadEach<TablePricingStructuresTariffs>(reader::load)

}
