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
import com.zepben.evolve.database.sqlite.cim.tables.tableCimVersion
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.BaseServiceComparator
import com.zepben.evolve.services.common.Resolvers
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.common.meta.MetadataCollection
import com.zepben.evolve.services.common.testdata.SchemaServices
import com.zepben.evolve.services.common.testdata.fillFieldsCommon
import com.zepben.evolve.services.customer.CustomerService
import com.zepben.evolve.services.customer.CustomerServiceComparator
import com.zepben.evolve.services.customer.testdata.fillFields
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.DriverManager

class CustomerDatabaseSchemaTest {

    @JvmField
    @RegisterExtension
    val systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val logger = LoggerFactory.getLogger(javaClass)
    private val schemaTestFile = "src/test/data/schemaTest.sqlite"

    @BeforeEach
    fun beforeEach() {
        Files.deleteIfExists(Paths.get(schemaTestFile))
    }

    @AfterEach
    fun afterEach() {
        Files.deleteIfExists(Paths.get(schemaTestFile))
    }

    @Test
    @Disabled
    fun loadRealFile() {
        systemErr.unmute()

        // Put the name of the database you want to load in src/test/resources/test-customer-database.txt
        val databaseFile = Files.readString(Path.of("src", "test", "resources", "test-customer-database.txt")).trim().trim('"')

        assertThat("database must exist", Files.exists(Paths.get(databaseFile)))

        val metadata = MetadataCollection()
        val customerService = CustomerService()

        DriverManager.getConnection("jdbc:sqlite:$databaseFile").use { connection ->
            assertThat("Database should have loaded", CustomerDatabaseReader(connection, metadata, customerService, databaseFile).load())
        }

        logger.info("Sleeping...")
        try {
            Thread.sleep(Long.MAX_VALUE)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Test
    fun `test schema for each supported type`() {
        /************ IEC61968 CUSTOMERS ************/
        validateSchema(SchemaServices.customerServicesOf(::Customer, Customer::fillFields))
        validateSchema(SchemaServices.customerServicesOf(::CustomerAgreement, CustomerAgreement::fillFields))
        validateSchema(SchemaServices.customerServicesOf(::PricingStructure, PricingStructure::fillFields))
        validateSchema(SchemaServices.customerServicesOf(::Tariff, Tariff::fillFields))

        /************ IEC61968 COMMON ************/
        validateSchema(SchemaServices.customerServicesOf(::Organisation, Organisation::fillFieldsCommon))

    }

    @Test
    fun testMetadataDataSourceSchema() {
        validateSchema(expectedMetadata = SchemaServices.createDataSourceTestServices())
    }

    @Test
    fun `test Name and NameType schema`() {
        validateSchema(SchemaServices.createNameTestService<CustomerService, Customer>())
    }

    @Test
    internal fun `post process fails with unresolved references`() {
        val metadata = MetadataCollection()
        val customerService = CustomerService().apply {
            // Add an unresolved reference that should trigger the post load check.
            resolveOrDeferReference(Resolvers.tariffs(PricingStructure("ps1")), "tar1")
        }

        // We save the database to get a valid schema to read, even though there is no data to write.
        CustomerDatabaseWriter(schemaTestFile, metadata, customerService).save()

        assertThat("Load should have failed", !readDatabase(metadata, customerService))

        assertThat(
            systemErr.log,
            containsString(
                "Unresolved references found in customer service after load - this should not occur. " +
                    "Failing reference was from PricingStructure ps1 resolving Tariff tar1"
            )
        )
    }

    @Test
    internal fun `check for error on duplicate id added to customer service`() {
        val writeServices = CustomerService()
        val readServices = CustomerService()

        val customer = Customer("customer1")
        writeServices.add(customer)
        readServices.add(customer)

        assertThat("write should have succeed", CustomerDatabaseWriter(schemaTestFile, MetadataCollection(), writeServices).save())

        if (!Files.exists(Paths.get(schemaTestFile)))
            error("Failed to save the schema test database.")

        systemErr.clearCapturedLog()
        assertThat("read should have failed", !readDatabase(MetadataCollection(), readServices))
        assertThat(
            systemErr.log,
            containsString("Failed to load ${customer.typeNameAndMRID()}. Unable to add to service '${readServices.name}': duplicate MRID")
        )
    }

    private fun validateSchema(expectedService: CustomerService = CustomerService(), expectedMetadata: MetadataCollection = MetadataCollection()) {
        systemErr.clearCapturedLog()

        assertThat("Database should have been saved", CustomerDatabaseWriter(schemaTestFile, expectedMetadata, expectedService).save())

        assertThat(systemErr.log, containsString("Creating database schema v${tableCimVersion.supportedVersion}"))
        assertThat("Database should now exist", Files.exists(Paths.get(schemaTestFile)))

        val customerService = CustomerService()
        val metadata = MetadataCollection()

        assertThat(" Database should have loaded", readDatabase(metadata, customerService))

        validateMetadata(metadata, expectedMetadata)
        validateService(customerService, expectedService) { CustomerServiceComparator() }
    }

    private fun validateMetadata(metadata: MetadataCollection, expectedMetadataCollection: MetadataCollection) {
        assertThat(metadata.dataSources, containsInAnyOrder(*expectedMetadataCollection.dataSources.toTypedArray()))
    }

    private fun validateService(
        service: BaseService,
        expectedService: BaseService,
        getComparator: () -> BaseServiceComparator
    ) {
        val differences = getComparator().compare(service, expectedService)

        if (differences.modifications().isNotEmpty())
            System.err.println(differences.toString())

        assertThat("unexpected objects found in loaded service", differences.missingFromTarget(), empty())
        assertThat("unexpected modifications", differences.modifications(), anEmptyMap())
        assertThat("objects missing from loaded service", differences.missingFromSource(), empty())
    }

    private fun CustomerDatabaseSchemaTest.readDatabase(metadata: MetadataCollection, service: CustomerService): Boolean =
        DriverManager.getConnection("jdbc:sqlite:$schemaTestFile").use { connection ->
            CustomerDatabaseReader(connection, metadata, service, schemaTestFile).load()
        }

}
