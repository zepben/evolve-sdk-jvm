/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.customer

import com.zepben.evolve.database.sqlite.common.cim.CimDatabaseTables
import com.zepben.evolve.database.sqlite.tables.SqliteTable
import com.zepben.evolve.database.sqlite.tables.associations.TableCustomerAgreementsPricingStructures
import com.zepben.evolve.database.sqlite.tables.associations.TablePricingStructuresTariffs
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TableOrganisations
import com.zepben.evolve.database.sqlite.tables.iec61968.customers.TableCustomerAgreements
import com.zepben.evolve.database.sqlite.tables.iec61968.customers.TableCustomers
import com.zepben.evolve.database.sqlite.tables.iec61968.customers.TablePricingStructures
import com.zepben.evolve.database.sqlite.tables.iec61968.customers.TableTariffs

/**
 * The collection of tables for our customer databases.
 */
class CustomerDatabaseTables : CimDatabaseTables() {

    override val includedTables: Sequence<SqliteTable> =
        super.includedTables + sequenceOf(
            TableCustomerAgreements(),
            TableCustomerAgreementsPricingStructures(),
            TableCustomers(),
            TableOrganisations(),
            TablePricingStructures(),
            TablePricingStructuresTariffs(),
            TableTariffs(),
        )

}
