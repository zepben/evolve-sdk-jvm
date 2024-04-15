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
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableIdentifiedObjects

@Suppress("PropertyName")
class TableDiagrams : TableIdentifiedObjects() {

    val DIAGRAM_STYLE: Column = Column(++columnIndex, "diagram_style", "TEXT", NOT_NULL)
    val ORIENTATION_KIND: Column = Column(++columnIndex, "orientation_kind", "TEXT", NOT_NULL)

    override val name: String = "diagrams"

}
