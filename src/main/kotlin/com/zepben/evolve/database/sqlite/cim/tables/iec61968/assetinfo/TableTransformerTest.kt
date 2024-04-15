/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.assetinfo

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets.TableAssetInfo

@Suppress("PropertyName")
abstract class TableTransformerTest : TableAssetInfo() {

    val BASE_POWER: Column = Column(++columnIndex, "base_power", "INTEGER", NULL)
    val TEMPERATURE: Column = Column(++columnIndex, "temperature", "NUMBER", NULL)

}
