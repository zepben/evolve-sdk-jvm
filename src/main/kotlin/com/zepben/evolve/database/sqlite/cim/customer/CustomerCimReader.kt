/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.customer

import com.zepben.evolve.cim.iec61968.common.Agreement
import com.zepben.evolve.cim.iec61968.customers.*
import com.zepben.evolve.database.sqlite.cim.CimReader
import com.zepben.evolve.database.sqlite.cim.tables.associations.TableCustomerAgreementsPricingStructures
import com.zepben.evolve.database.sqlite.cim.tables.associations.TablePricingStructuresTariffs
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TableAgreements
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.customers.TableCustomerAgreements
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.customers.TableCustomers
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.customers.TablePricingStructures
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.customers.TableTariffs
import com.zepben.evolve.database.sqlite.extensions.getNullableInt
import com.zepben.evolve.database.sqlite.extensions.getNullableString
import com.zepben.evolve.services.common.extensions.ensureGet
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.customer.CustomerService
import java.sql.ResultSet
import java.sql.SQLException

/**
 * A class for reading the [CustomerService] tables from the database.
 *
 * @property service The [CustomerService] to populate from the database.
 */
class CustomerCimReader(
    override val service: CustomerService
) : CimReader(service) {

    // ###################
    // # IEC61968 Common #
    // ###################

    @Throws(SQLException::class)
    private fun loadAgreement(agreement: Agreement, table: TableAgreements, resultSet: ResultSet): Boolean {
        return loadDocument(agreement, table, resultSet)
    }

    // ######################
    // # IEC61968 Customers #
    // ######################

    /**
     * Create a [Customer] and populate its fields from [TableCustomers].
     *
     * @param table The database table to read the [Customer] fields from.
     * @param resultSet The record in the database table containing the fields for this [Customer].
     * @param setIdentifier A callback to register the mRID of this [Customer] for logging purposes.
     *
     * @return true if the [Customer] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableCustomers, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val customer = Customer(setIdentifier(resultSet.getString(table.MRID.queryIndex))).apply {
            kind = CustomerKind.valueOf(resultSet.getString(table.KIND.queryIndex))
            numEndDevices = resultSet.getNullableInt(table.NUM_END_DEVICES.queryIndex)
            specialNeed = resultSet.getNullableString(table.SPECIAL_NEED.queryIndex)
        }

        return loadOrganisationRole(customer, table, resultSet) && service.addOrThrow(customer)
    }

    /**
     * Create a [CustomerAgreement] and populate its fields from [TableCustomerAgreements].
     *
     * @param table The database table to read the [CustomerAgreement] fields from.
     * @param resultSet The record in the database table containing the fields for this [CustomerAgreement].
     * @param setIdentifier A callback to register the mRID of this [CustomerAgreement] for logging purposes.
     *
     * @return true if the [CustomerAgreement] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableCustomerAgreements, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val customerAgreement = CustomerAgreement(setIdentifier(resultSet.getString(table.MRID.queryIndex))).apply {
            customer = service.ensureGet(resultSet.getString(table.CUSTOMER_MRID.queryIndex), typeNameAndMRID())
            customer?.addAgreement(this)
        }

        return loadAgreement(customerAgreement, table, resultSet) && service.addOrThrow(customerAgreement)
    }

    /**
     * Create a [PricingStructure] and populate its fields from [TablePricingStructures].
     *
     * @param table The database table to read the [PricingStructure] fields from.
     * @param resultSet The record in the database table containing the fields for this [PricingStructure].
     * @param setIdentifier A callback to register the mRID of this [PricingStructure] for logging purposes.
     *
     * @return true if the [PricingStructure] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TablePricingStructures, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val pricingStructure = PricingStructure(setIdentifier(resultSet.getString(table.MRID.queryIndex)))

        return loadDocument(pricingStructure, table, resultSet) && service.addOrThrow(pricingStructure)
    }

    /**
     * Create a [Tariff] and populate its fields from [TableTariffs].
     *
     * @param table The database table to read the [Tariff] fields from.
     * @param resultSet The record in the database table containing the fields for this [Tariff].
     * @param setIdentifier A callback to register the mRID of this [Tariff] for logging purposes.
     *
     * @return true if the [Tariff] was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableTariffs, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val tariff = Tariff(setIdentifier(resultSet.getString(table.MRID.queryIndex)))

        return loadDocument(tariff, table, resultSet) && service.addOrThrow(tariff)
    }

    // ################
    // # Associations #
    // ################

    /**
     * Create a [CustomerAgreement] to [PricingStructure] association from [TableCustomerAgreementsPricingStructures].
     *
     * @param table The database table to read the association from.
     * @param resultSet The record in the database table containing the fields for this association.
     * @param setIdentifier A callback to register the identifier of this association for logging purposes.
     *
     * @return true if the association was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TableCustomerAgreementsPricingStructures, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val customerAgreementMRID = setIdentifier(resultSet.getString(table.CUSTOMER_AGREEMENT_MRID.queryIndex))
        setIdentifier("${customerAgreementMRID}-to-UNKNOWN")

        val pricingStructureMRID = resultSet.getString(table.PRICING_STRUCTURE_MRID.queryIndex)
        val id = setIdentifier("${customerAgreementMRID}-to-${pricingStructureMRID}")

        val typeNameAndMRID = "customer agreement to pricing structure association $id"
        val customerAgreement = service.ensureGet<CustomerAgreement>(customerAgreementMRID, typeNameAndMRID)
        val pricingStructure = service.ensureGet<PricingStructure>(pricingStructureMRID, typeNameAndMRID)

        pricingStructure?.let { customerAgreement?.addPricingStructure(it) }

        return true
    }

    /**
     * Create a [PricingStructure] to [Tariff] association from [TablePricingStructuresTariffs].
     *
     * @param table The database table to read the association from.
     * @param resultSet The record in the database table containing the fields for this association.
     * @param setIdentifier A callback to register the identifier of this association for logging purposes.
     *
     * @return true if the association was successfully read from the database and added to the service.
     * @throws SQLException For any errors encountered reading from the database.
     */
    @Throws(SQLException::class)
    fun load(table: TablePricingStructuresTariffs, resultSet: ResultSet, setIdentifier: (String) -> String): Boolean {
        val pricingStructureMRID = setIdentifier(resultSet.getString(table.PRICING_STRUCTURE_MRID.queryIndex))
        setIdentifier("${pricingStructureMRID}-to-UNKNOWN")

        val tariffMRID = resultSet.getString(table.TARIFF_MRID.queryIndex)
        val id = setIdentifier("${pricingStructureMRID}-to-${tariffMRID}")

        val typeNameAndMRID = "pricing structure to tariff association $id"
        val pricingStructure = service.ensureGet<PricingStructure>(pricingStructureMRID, typeNameAndMRID)
        val tariff = service.ensureGet<Tariff>(tariffMRID, typeNameAndMRID)

        tariff?.let { pricingStructure?.addTariff(it) }

        return true
    }

}
