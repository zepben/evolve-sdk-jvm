/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61968.metering

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets.TableAssetContainers

@Suppress("PropertyName")
abstract class TableEndDevices : TableAssetContainers() {

    val CUSTOMER_MRID: Column = Column(++columnIndex, "customer_mrid", "TEXT", NULL)
    val SERVICE_LOCATION_MRID: Column = Column(++columnIndex, "service_location_mrid", "TEXT", NULL)

}
