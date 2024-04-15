/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets.combined

import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.ChangeSetValidator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import java.sql.Statement

object ChangeSet46Validator : ChangeSetValidator {
    override fun setUpStatements(): List<String> = listOf(
        "INSERT INTO potential_transformers (" +
            "mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, terminal_mrid, " +
            "potential_transformer_info_mrid, type) VALUES ('id1', 'name', 'desc', 1, 'l_id', 2, true, false, 't_id', 'pti_id', NULL)",
        "INSERT INTO potential_transformers (" +
            "mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, terminal_mrid, " +
            "potential_transformer_info_mrid, type) VALUES ('id2', 'name', 'desc', 1, 'l_id', 2, true, false, 't_id', 'pti_id', 'T1')"
    )

    override fun populateStatements(): List<String> = listOf(
        "INSERT INTO potential_transformers (" +
            "mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, terminal_mrid, " +
            "potential_transformer_info_mrid, type) VALUES ('id3', 'name', 'desc', 1, 'l_id', 2, true, false, 't_id', 'pti_id', 'T2')"
    )

    override fun validate(statement: Statement) {
        ensureIndexes(
            statement,
            "potential_transformers_mrid",
            "potential_transformers_name"
        )
        validateRows(statement, "SELECT * FROM potential_transformers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("terminal_mrid"), equalTo("t_id"))
                assertThat(rs.getString("potential_transformer_info_mrid"), equalTo("pti_id"))
                assertThat(rs.getString("type"), equalTo("UNKNOWN"))
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("terminal_mrid"), equalTo("t_id"))
                assertThat(rs.getString("potential_transformer_info_mrid"), equalTo("pti_id"))
                assertThat(rs.getString("type"), equalTo("T1"))
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id3"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("terminal_mrid"), equalTo("t_id"))
                assertThat(rs.getString("potential_transformer_info_mrid"), equalTo("pti_id"))
                assertThat(rs.getString("type"), equalTo("T2"))
            }
        )
    }

    override fun tearDownStatements(): List<String> = listOf(
        "DELETE FROM potential_transformers"
    )

}
