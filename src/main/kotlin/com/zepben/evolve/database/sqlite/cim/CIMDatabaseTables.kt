/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim

import com.zepben.evolve.database.sqlite.cim.tables.SqliteTable
import com.zepben.evolve.database.sqlite.cim.tables.TableCimVersion
import com.zepben.evolve.database.sqlite.cim.tables.TableMetadataDataSources
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableNameTypes
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.TableNames
import com.zepben.evolve.database.sqlite.common.BaseDatabaseTables

/**
 * The base collection of tables for all our CIM databases.
 */
open class CimDatabaseTables : BaseDatabaseTables() {

    override val includedTables: Sequence<SqliteTable> = sequenceOf(
        TableCimVersion,
        TableMetadataDataSources(),
        TableNameTypes(),
        TableNames()
    )

}
