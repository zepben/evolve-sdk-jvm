/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.diagram

import com.zepben.evolve.database.sqlite.common.cim.CimDatabaseTables
import com.zepben.evolve.database.sqlite.tables.SqliteTable
import com.zepben.evolve.database.sqlite.tables.iec61970.base.diagramlayout.TableDiagramObjectPoints
import com.zepben.evolve.database.sqlite.tables.iec61970.base.diagramlayout.TableDiagramObjects
import com.zepben.evolve.database.sqlite.tables.iec61970.base.diagramlayout.TableDiagrams

/**
 * The collection of tables for our diagram databases.
 */
class DiagramDatabaseTables : CimDatabaseTables() {

    override val includedTables: Sequence<SqliteTable> =
        super.includedTables + sequenceOf(
            TableDiagramObjectPoints(),
            TableDiagramObjects(),
            TableDiagrams()
        )

}
