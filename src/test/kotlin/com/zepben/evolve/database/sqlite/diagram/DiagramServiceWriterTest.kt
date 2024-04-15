/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.diagram

import com.zepben.evolve.cim.iec61970.base.diagramlayout.Diagram
import com.zepben.evolve.database.sqlite.cim.diagram.DiagramCimWriter
import com.zepben.evolve.database.sqlite.cim.diagram.DiagramServiceWriter
import com.zepben.evolve.services.diagram.DiagramService
import com.zepben.testutils.junit.SystemLogExtension
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class DiagramServiceWriterTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val diagramService = DiagramService()
    private val cimWriter = mockk<DiagramCimWriter> { every { save(any<Diagram>()) } returns true }
    private val diagramServiceWriter = DiagramServiceWriter(diagramService, mockk(), cimWriter)

    //
    // NOTE: We don't do an exhaustive test of saving objects as this is done via the schema test.
    //

    @Test
    internal fun `passes objects through to the cim writer`() {
        val diagram = Diagram().also { diagramService.add(it) }

        // NOTE: the save method will fail due to the relaxed mock returning false for all save operations,
        //       but a save should still be attempted on every object
        diagramServiceWriter.save()

        verify(exactly = 1) { cimWriter.save(diagram) }
    }

}
