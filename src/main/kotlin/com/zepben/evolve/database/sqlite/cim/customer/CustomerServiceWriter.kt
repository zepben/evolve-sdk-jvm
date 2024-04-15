/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.customer

import com.zepben.evolve.cim.iec61968.common.Organisation
import com.zepben.evolve.cim.iec61968.customers.Customer
import com.zepben.evolve.cim.iec61968.customers.CustomerAgreement
import com.zepben.evolve.cim.iec61968.customers.PricingStructure
import com.zepben.evolve.cim.iec61968.customers.Tariff
import com.zepben.evolve.database.sqlite.common.BaseServiceWriter
import com.zepben.evolve.services.customer.CustomerService

/**
 * A class for writing a [CustomerService] into the database.
 *
 * @param service The [CustomerService] to save to the database.
 * @param databaseTables The [CustomerDatabaseTables] to add to the database.
 */
class CustomerServiceWriter @JvmOverloads constructor(
    override val service: CustomerService,
    databaseTables: CustomerDatabaseTables,
    override val writer: CustomerCimWriter = CustomerCimWriter(databaseTables)
) : BaseServiceWriter(service, writer) {

    override fun doSave(): Boolean =
        saveEach<Organisation>(writer::save)
            .andSaveEach<Customer>(writer::save)
            .andSaveEach<CustomerAgreement>(writer::save)
            .andSaveEach<PricingStructure>(writer::save)
            .andSaveEach<Tariff>(writer::save)

}
