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

abstract class MetricsSchemaTest {

    @JvmField
    @RegisterExtension
    val systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val schemaTestFile = "src/test/data/schemaTest.sqlite"
    private val uuidString = "0e22c8de-7b3a-4a57-92c2-ffbf973a05d2"
    private val uuid = UUID.fromString(uuidString)

    @BeforeEach
    protected fun beforeEach() {
        Files.deleteIfExists(Paths.get(schemaTestFile))
    }

    @AfterEach
    protected fun afterEach() {
        Files.deleteIfExists(Paths.get(schemaTestFile))
    }

    @Test
    internal fun `test schema`() {
        validateJob(
            baseJob(),
            "jobs",
            listOf(uuidString, Instant.EPOCH.toString(), "source", "application", "applicationVersion")
        )
        validateJob(
            baseJob().apply {
                sources["abc"].timestamp = Instant.EPOCH
                sources["abc"].fileHash = "xyz".toByteArray()
            },
            "job_sources",
            listOf(uuidString, "abc", Instant.EPOCH.toString(), "xyz".toByteArray())
        )
        validateJob(
            baseJob().apply {
                networkMetrics[TotalNetworkContainer]["abc"] = 1.2
            },
            "network_container_metrics",
            listOf(uuidString, "GLOBAL", "", "TOTAL", "abc", 1.2)
        )
        validateJob(
            baseJob().apply {
                networkMetrics[PartialNetworkContainer(NetworkLevel.Feeder, "fdr", "feeder")]["abc"] = 1.2
            },
            "network_container_metrics",
            listOf(uuidString, "fdr", "feeder", "Feeder", "abc", 1.2)
        )

        // Ensure the network metrics table can handle Feeder & FeederTotal for same feeder and metric, same with Substation and SubstationTotal
        validateJob(
            baseJob().apply {
                networkMetrics[PartialNetworkContainer(NetworkLevel.Feeder, "fdr", "feeder")]["abc"] = 1.2
                networkMetrics[PartialNetworkContainer(NetworkLevel.FeederTotal, "fdr", "feeder")]["abc"] = 1.3
            },
            "network_container_metrics",
            listOf(uuidString, "fdr", "feeder", "Feeder", "abc", 1.2),
            listOf(uuidString, "fdr", "feeder", "FeederTotal", "abc", 1.3)
        )
        validateJob(
            baseJob().apply {
                networkMetrics[PartialNetworkContainer(NetworkLevel.Substation, "sub", "substation")]["abc"] = 1.2
                networkMetrics[PartialNetworkContainer(NetworkLevel.SubstationTotal, "sub", "substation")]["abc"] = 1.3
            },
            "network_container_metrics",
            listOf(uuidString, "sub", "substation", "Substation", "abc", 1.2),
            listOf(uuidString, "sub", "substation", "SubstationTotal", "abc", 1.3),
        )
    }

    private fun baseJob() = IngestionJob(uuid, metadata = IngestionMetadata(Instant.EPOCH, "source", "application", "applicationVersion"))

    private fun validateJob(expectedJob: IngestionJob, tableName: String, vararg rows: List<Any>) {
        systemErr.clearCapturedLog()

        assertThat("Database should have been saved", save(schemaTestFile, expectedJob))

        assertThat(systemErr.log, Matchers.containsString("Creating database schema v${tableMetricsVersion.supportedVersion}"))
        assertThat("Database should now exist", Files.exists(Paths.get(schemaTestFile)))

        DriverManager.getConnection("jdbc:sqlite:$schemaTestFile").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT * FROM $tableName").use { rs ->
                    rows.forEachIndexed { i, values ->
                        assertThat("Row no. ${i + 1} should exist for $tableName", rs.next())
                        values.forEachIndexed { j, value ->
                            assertThat(rs.getObject(j + 1), equalTo(value))
                        }
                    }
                }
            }
        }

        // Needed, as the metrics database is persistent
        Files.deleteIfExists(Paths.get(schemaTestFile))
    }

    abstract fun save(file: String, job: IngestionJob): Boolean

}
