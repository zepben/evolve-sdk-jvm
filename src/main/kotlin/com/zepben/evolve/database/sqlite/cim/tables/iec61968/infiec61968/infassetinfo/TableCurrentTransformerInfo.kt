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
class TableCurrentTransformerInfo : TableAssetInfo() {

    val ACCURACY_CLASS: Column = Column(++columnIndex, "accuracy_class", "TEXT", NULL)
    val ACCURACY_LIMIT: Column = Column(++columnIndex, "accuracy_limit", "NUMBER", NULL)
    val CORE_COUNT: Column = Column(++columnIndex, "core_count", "INTEGER", NULL)
    val CT_CLASS: Column = Column(++columnIndex, "ct_class", "TEXT", NULL)
    val KNEE_POINT_VOLTAGE: Column = Column(++columnIndex, "knee_point_voltage", "INTEGER", NULL)
    val MAX_RATIO_DENOMINATOR: Column = Column(++columnIndex, "max_ratio_denominator", "NUMBER", NULL)
    val MAX_RATIO_NUMERATOR: Column = Column(++columnIndex, "max_ratio_numerator", "NUMBER", NULL)
    val NOMINAL_RATIO_DENOMINATOR: Column = Column(++columnIndex, "nominal_ratio_denominator", "NUMBER", NULL)
    val NOMINAL_RATIO_NUMERATOR: Column = Column(++columnIndex, "nominal_ratio_numerator", "NUMBER", NULL)
    val PRIMARY_RATIO: Column = Column(++columnIndex, "primary_ratio", "NUMBER", NULL)
    val RATED_CURRENT: Column = Column(++columnIndex, "rated_current", "INTEGER", NULL)
    val SECONDARY_FLS_RATING: Column = Column(++columnIndex, "secondary_fls_rating", "INTEGER", NULL)
    val SECONDARY_RATIO: Column = Column(++columnIndex, "secondary_ratio", "NUMBER", NULL)
    val USAGE: Column = Column(++columnIndex, "usage", "TEXT", NULL)

    override val name: String = "current_transformer_info"

}
