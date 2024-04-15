/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.diagramlayout

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NOT_NULL
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableIdentifiedObjects

@Suppress("PropertyName")
class TableDiagramObjects : TableIdentifiedObjects() {

    val IDENTIFIED_OBJECT_MRID: Column = Column(++columnIndex, "identified_object_mrid", "TEXT", NULL)
    val DIAGRAM_MRID: Column = Column(++columnIndex, "diagram_mrid", "TEXT", NULL)
    val STYLE: Column = Column(++columnIndex, "style", "TEXT", NULL)
    val ROTATION: Column = Column(++columnIndex, "rotation", "NUMBER", NOT_NULL)

    override val name: String = "diagram_objects"

    override val nonUniqueIndexColumns: MutableList<List<Column>> =
        super.nonUniqueIndexColumns.apply {
            add(listOf(IDENTIFIED_OBJECT_MRID))
            add(listOf(DIAGRAM_MRID))
        }

}
