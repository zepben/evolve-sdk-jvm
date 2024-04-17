/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.extensions

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

@Throws(SQLException::class)
fun PreparedStatement.executeConfiguredQuery(): ResultSet {
    this.queryTimeout = 30
    this.fetchSize = 10000

    val results = this.executeQuery()
    if (!results.isClosed)
        results.fetchDirection = ResultSet.FETCH_FORWARD

    return results
}
