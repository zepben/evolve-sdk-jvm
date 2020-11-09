/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.cimbend.database.sqlite.tables.iec61968.metering;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.cimbend.database.Column;
import com.zepben.cimbend.database.sqlite.tables.iec61970.base.core.TableIdentifiedObjects;

import static com.zepben.cimbend.database.Column.Nullable.NULL;

@EverythingIsNonnullByDefault
public class TableUsagePoints extends TableIdentifiedObjects {

    public final Column LOCATION_MRID = new Column(++columnIndex, "location_mrid", "TEXT", NULL);

    @Override
    public String name() {
        return "usage_points";
    }

    @Override
    protected Class<?> getTableClass() {
        return TableUsagePoints.class;
    }

    @Override
    protected Object getTableClassInstance() {
        return this;
    }

}