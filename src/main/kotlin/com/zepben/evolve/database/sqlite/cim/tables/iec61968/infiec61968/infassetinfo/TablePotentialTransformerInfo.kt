/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.infiec61968.infassetinfo

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets.TableAssetInfo

@Suppress("PropertyName")
class TablePotentialTransformerInfo : TableAssetInfo() {

    val ACCURACY_CLASS: Column = Column(++columnIndex, "accuracy_class", "TEXT", NULL)
    val NOMINAL_RATIO_DENOMINATOR: Column = Column(++columnIndex, "nominal_ratio_denominator", "NUMBER", NULL)
    val NOMINAL_RATIO_NUMERATOR: Column = Column(++columnIndex, "nominal_ratio_numerator", "NUMBER", NULL)
    val PRIMARY_RATIO: Column = Column(++columnIndex, "primary_ratio", "NUMBER", NULL)
    val PT_CLASS: Column = Column(++columnIndex, "pt_class", "TEXT", NULL)
    val RATED_VOLTAGE: Column = Column(++columnIndex, "rated_voltage", "INTEGER", NULL)
    val SECONDARY_RATIO: Column = Column(++columnIndex, "secondary_ratio", "NUMBER", NULL)

    override val name: String = "potential_transformer_info"

}
