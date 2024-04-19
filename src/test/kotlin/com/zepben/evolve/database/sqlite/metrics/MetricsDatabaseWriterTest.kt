package com.zepben.evolve.database.sqlite.metrics

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.sql.Connection

internal class MetricsDatabaseWriterTest {

    private val connection = mockk<Connection>()
    private val writer = mockk<MetricsWriter> {
        every { save() } returns true
    }

    @Test
    internal fun callsWriter() {
        val result = MetricsDatabaseWriter(
            "databaseFile",
            mockk(), // ingestion job isn't actually used to create the MetricsWriter
            createMetricsWriter = { conn ->
                assertThat(conn, equalTo(connection))
                writer
            }
        ).saveWithConnection(connection)

        assertThat("Should have saved successfully", result)

        verify { writer.save() }
    }

}
