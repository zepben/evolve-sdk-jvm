/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.common.cim

import com.zepben.evolve.database.sqlite.common.BaseDatabaseTables
import com.zepben.evolve.database.sqlite.tables.SqliteTable
import com.zepben.evolve.database.sqlite.tables.TableMetadataDataSources
import com.zepben.evolve.database.sqlite.tables.TableVersion
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.TableNameTypes
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.TableNames

/**
 * The base collection of tables for all our CIM databases.
 */
open class CimDatabaseTables : BaseDatabaseTables() {

    override val includedTables: Sequence<SqliteTable> = sequenceOf(
        TableMetadataDataSources(),
        TableVersion(),
        TableNameTypes(),
        TableNames()
    )

}
