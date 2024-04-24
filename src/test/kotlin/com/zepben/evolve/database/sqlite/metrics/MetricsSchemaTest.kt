/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.metrics

import com.zepben.evolve.database.sqlite.metrics.tables.tableMetricsVersion
import com.zepben.evolve.metrics.*
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.DriverManager
import java.time.Instant
import java.util.*

internal class MetricsSchemaTest {

    @JvmField
    @RegisterExtension
    val systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val schemaTestFile = "src/test/data/schemaTest.sqlite"
    private val uuidString = "0e22c8de-7b3a-4a57-92c2-ffbf973a05d2"
    private val uuid = UUID.fromString(uuidString)

    @BeforeEach
    private fun beforeEach() {
        Files.deleteIfExists(Paths.get(schemaTestFile))
    }

    @AfterEach
    private fun afterEach() {
        Files.deleteIfExists(Paths.get(schemaTestFile))
    }

    @Test
    internal fun `test schema for each supported type`() {
        validateJob(
            IngestionJob(uuid, metadata = IngestionMetadata(Instant.EPOCH, "source", "application", "applicationVersion")),
            "jobs",
            listOf(uuidString, Instant.EPOCH.toString(), "source", "application", "applicationVersion")
        )
        validateJob(
            IngestionJob(uuid).apply {
                sources["abc"].timestamp = Instant.EPOCH
                sources["abc"].fileHash = "xyz".toByteArray()
            },
            "job_sources",
            listOf(uuidString, "abc", Instant.EPOCH.toString(), "xyz".toByteArray())
        )
        validateJob(
            IngestionJob(uuid).apply {
                networkMetrics[TotalNetworkContainer]["abc"] = 1.2
            },
            "network_container_metrics",
            listOf(uuidString, "GLOBAL", "", "TOTAL", "abc", 1.2)
        )
        validateJob(
            IngestionJob(uuid).apply {
                networkMetrics[PartialNetworkContainer(NetworkLevel.Feeder, "fdr", "feeder")]["abc"] = 1.2
            },
            "network_container_metrics",
            listOf(uuidString, "fdr", "feeder", "Feeder", "abc", 1.2)
        )

    }

    private fun validateJob(expectedJob: IngestionJob, tableName: String, values: List<Any>) {
        systemErr.clearCapturedLog()

        assertThat("Database should have been saved", MetricsDatabaseWriter(schemaTestFile, expectedJob).save())

        assertThat(systemErr.log, Matchers.containsString("Creating database schema v${tableMetricsVersion.supportedVersion}"))
        assertThat("Database should now exist", Files.exists(Paths.get(schemaTestFile)))

        DriverManager.getConnection("jdbc:sqlite:$schemaTestFile").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT * FROM $tableName").use { rs ->
                    assertThat("Row should exist for $tableName", rs.next())
                    values.forEachIndexed { i, value ->
                        assertThat(rs.getObject(i + 1), equalTo(value))
                    }
                }
            }
        }
    }

}
