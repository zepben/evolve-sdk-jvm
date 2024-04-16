/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.diagram

import com.zepben.evolve.cim.iec61970.base.diagramlayout.Diagram
import com.zepben.evolve.cim.iec61970.base.diagramlayout.DiagramObject
import com.zepben.evolve.database.sqlite.cim.BaseServiceWriter
import com.zepben.evolve.services.diagram.DiagramService

/**
 * A class for writing a [DiagramService] into the database.
 *
 * @param service The [DiagramService] to save to the database.
 * @param databaseTables The [DiagramDatabaseTables] to add to the database.
 */
class DiagramServiceWriter @JvmOverloads constructor(
    override val service: DiagramService,
    databaseTables: DiagramDatabaseTables,
    override val writer: DiagramCimWriter = DiagramCimWriter(databaseTables)
) : BaseServiceWriter(service, writer) {

    override fun doSave(): Boolean =
        saveEach<DiagramObject>(writer::save)
            .andSaveEach<Diagram>(writer::save)

}
