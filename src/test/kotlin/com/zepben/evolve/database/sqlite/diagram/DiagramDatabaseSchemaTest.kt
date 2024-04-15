/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.diagram

import com.zepben.evolve.cim.iec61970.base.diagramlayout.Diagram
import com.zepben.evolve.cim.iec61970.base.diagramlayout.DiagramObject
import com.zepben.evolve.database.sqlite.cim.diagram.DiagramDatabaseReader
import com.zepben.evolve.database.sqlite.cim.diagram.DiagramDatabaseWriter
import com.zepben.evolve.database.sqlite.cim.tables.TableVersion
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.BaseServiceComparator
import com.zepben.evolve.services.common.Resolvers
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.common.meta.MetadataCollection
import com.zepben.evolve.services.common.testdata.SchemaServices
import com.zepben.evolve.services.diagram.DiagramService
import com.zepben.evolve.services.diagram.DiagramServiceComparator
import com.zepben.evolve.services.diagram.testdata.fillFields
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

class DiagramDatabaseSchemaTest {

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

        // Put the name of the database you want to load in src/test/resources/test-diagram-database.txt
        val databaseFile = Files.readString(Path.of("src", "test", "resources", "test-diagram-database.txt")).trim().trim('"')

        assertThat("database must exist", Files.exists(Paths.get(databaseFile)))

        val metadata = MetadataCollection()
        val diagramService = DiagramService()

        DriverManager.getConnection("jdbc:sqlite:$databaseFile").use { connection ->
            assertThat("Database should have loaded", DiagramDatabaseReader(connection, metadata, diagramService, databaseFile).load())
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
        /************ IEC61970 BASE DIAGRAM LAYOUT ************/
        validateSchema(SchemaServices.diagramServicesOf(::Diagram, Diagram::fillFields))
        validateSchema(SchemaServices.diagramServicesOf(::DiagramObject, DiagramObject::fillFields))
    }

    @Test
    fun testMetadataDataSourceSchema() {
        validateSchema(expectedMetadata = SchemaServices.createDataSourceTestServices())
    }

    @Test
    fun `test Name and NameType schema`() {
        validateSchema(SchemaServices.createNameTestService<DiagramService, Diagram>())
    }

    @Test
    internal fun `post process fails with unresolved references`() {
        val metadata = MetadataCollection()
        val diagramService = DiagramService().apply {
            // Add an unresolved reference that should trigger the post load check.
            resolveOrDeferReference(Resolvers.diagram(DiagramObject("do1")), "diagram1")
        }

        // We save the database to get a valid schema to read, even though there is no data to write.
        DiagramDatabaseWriter(schemaTestFile, metadata, diagramService).save()

        assertThat("Load should have failed", !readDatabase(metadata, diagramService))

        assertThat(
            systemErr.log,
            containsString(
                "Unresolved references found in diagram service after load - this should not occur. " +
                    "Failing reference was from DiagramObject do1 resolving Diagram diagram1"
            )
        )
    }

    @Test
    internal fun `check for error on duplicate id added to diagram service`() {
        val writeServices = DiagramService()
        val readServices = DiagramService()

        val diagram = Diagram("diagram1")
        writeServices.add(diagram)
        readServices.add(diagram)

        assertThat("write should have succeed", DiagramDatabaseWriter(schemaTestFile, MetadataCollection(), writeServices).save())

        if (!Files.exists(Paths.get(schemaTestFile)))
            error("Failed to save the schema test database.")

        systemErr.clearCapturedLog()
        assertThat("read should have failed", !readDatabase(MetadataCollection(), readServices))
        assertThat(
            systemErr.log,
            containsString("Failed to load ${diagram.typeNameAndMRID()}. Unable to add to service '${readServices.name}': duplicate MRID")
        )
    }

    private fun validateSchema(expectedService: DiagramService = DiagramService(), expectedMetadata: MetadataCollection = MetadataCollection()) {
        systemErr.clearCapturedLog()

        assertThat("Database should have been saved", DiagramDatabaseWriter(schemaTestFile, expectedMetadata, expectedService).save())

        assertThat(systemErr.log, containsString("Creating database schema v${TableVersion().SUPPORTED_VERSION}"))
        assertThat("Database should now exist", Files.exists(Paths.get(schemaTestFile)))

        val diagramService = DiagramService()
        val metadata = MetadataCollection()

        assertThat(" Database should have loaded", readDatabase(metadata, diagramService))

        validateMetadata(metadata, expectedMetadata)
        validateService(diagramService, expectedService) { DiagramServiceComparator() }
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

    private fun DiagramDatabaseSchemaTest.readDatabase(metadata: MetadataCollection, service: DiagramService): Boolean =
        DriverManager.getConnection("jdbc:sqlite:$schemaTestFile").use { connection ->
            DiagramDatabaseReader(connection, metadata, service, schemaTestFile).load()
        }

}
