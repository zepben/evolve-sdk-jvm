/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.cimbend.database.sqlite.tables.iec61970.base.core;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.cimbend.database.Column;

import java.util.Collections;
import java.util.List;

import static com.zepben.cimbend.database.Column.Nullable.NULL;

@EverythingIsNonnullByDefault
public class TableSubGeographicalRegions extends TableIdentifiedObjects {

    public final Column GEOGRAPHICAL_REGION_MRID = new Column(++columnIndex, "geographical_region_mrid", "TEXT", NULL);

    @Override
    public String name() {
        return "sub_geographical_regions";
    }

    @Override
    public List<List<Column>> nonUniqueIndexColumns() {
        List<List<Column>> cols = super.nonUniqueIndexColumns();
        cols.add(Collections.singletonList(GEOGRAPHICAL_REGION_MRID));
        return cols;
    }

    @Override
    protected Class<?> getTableClass() {
        return TableSubGeographicalRegions.class;
    }

    @Override
    protected Object getTableClassInstance() {
        return this;
    }

}