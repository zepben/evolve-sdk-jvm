/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.diagram

import com.zepben.evolve.database.sqlite.cim.metadata.MetadataCollectionReader
import com.zepben.evolve.database.sqlite.common.TableVersion
import com.zepben.evolve.services.diagram.DiagramService
import com.zepben.testutils.junit.SystemLogExtension
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verifySequence
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.sql.Connection

internal class DiagramDatabaseReaderTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val databaseFile = "databaseFile"
    private val service = mockk<DiagramService>(relaxed = true)

    private val metadataReader = mockk<MetadataCollectionReader>().also { every { it.load() } returns true }
    private val diagramServiceReader = mockk<DiagramServiceReader>().also { every { it.load() } returns true }

    private val connection = mockk<Connection> {
        justRun { close() }
    }

    private val tableVersion = mockk<TableVersion> {
        every { getVersion(any()) } returns 1
        every { this@mockk.supportedVersion } returns 1
    }

    private val reader = DiagramDatabaseReader(
        connection,
        mockk(), // The metadata is unused if we provide a metadataReader.
        service,
        databaseFile,
        mockk(), // tables should not be used if we provide the rest of the parameters, so provide a mockk that will throw if used.
        metadataReader,
        diagramServiceReader,
        tableVersion
    )

    //
    // NOTE: We don't do an exhaustive test of reading objects as this is done via the schema test.
    //

    @Test
    internal fun `calls expected processors, including post processes`() {
        assertThat("Should have loaded", reader.load())

        verifySequence {
            tableVersion.supportedVersion
            tableVersion.getVersion(connection)

            metadataReader.load()
            diagramServiceReader.load()
            service.unresolvedReferences()
        }
    }

}
