/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.diagram

import com.zepben.evolve.database.sqlite.common.BaseServiceReader
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.diagramlayout.TableDiagramObjectPoints
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.diagramlayout.TableDiagramObjects
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.diagramlayout.TableDiagrams
import com.zepben.evolve.services.diagram.DiagramService
import java.sql.Connection

/**
 * A class for reading a [DiagramService] from the database.
 *
 * @param service The [DiagramService] to populate from the database.
 * @param databaseTables The tables available in the database.
 * @param connection A connection to the database.
 *
 * @property reader The [DiagramCimReader] used to load the objects from the database.
 */
class DiagramServiceReader @JvmOverloads constructor(
    service: DiagramService,
    databaseTables: DiagramDatabaseTables,
    connection: Connection,
    override val reader: DiagramCimReader = DiagramCimReader(service)
) : BaseServiceReader(databaseTables, connection, reader) {

    override fun doLoad(): Boolean =

        loadEach<TableDiagrams>(reader::load)
            .andLoadEach<TableDiagramObjects>(reader::load)
            .andLoadEach<TableDiagramObjectPoints>(reader::load)

}
