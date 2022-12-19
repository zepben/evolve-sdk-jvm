/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.tables.iec61968.assetinfo

import com.zepben.evolve.database.sqlite.tables.Column
import com.zepben.evolve.database.sqlite.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.TableAssetInfo

@Suppress("PropertyName")
class TableSwitchInfo : TableAssetInfo() {

    val RATED_INTERRUPTING_TIME = Column(++columnIndex, "rated_interrupting_time", "NUMBER", NULL)

    override fun name(): String {
        return "switch_info"
    }

    override val tableClass = this.javaClass
    override val tableClassInstance = this

}
