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

object ChangeSet44Validator : ChangeSetValidator {

    override fun setUpStatements(): List<String> = emptyList()

    override fun populateStatements(): List<String> = listOf(
        "INSERT INTO current_transformer_info (" +
            "mrid, name, description, num_diagram_objects, accuracy_class, accuracy_limit, core_count, ct_class, knee_point_voltage, max_ratio_denominator, " +
            "max_ratio_numerator, nominal_ratio_denominator, nominal_ratio_numerator, primary_ratio, rated_current, secondary_fls_rating, secondary_ratio, " +
            "usage) VALUES ('id', 'name', 'desc', 1, 'A', 2.2, 3, 'CT', 4, 5.5, 6.6, 7.7, 8.8, 9.9, 10, 11, 12.12, 'U')",
        "INSERT INTO potential_transformer_info (" +
            "mrid, name, description, num_diagram_objects, accuracy_class, nominal_ratio_denominator, nominal_ratio_numerator, primary_ratio, pt_class, " +
            "rated_voltage, secondary_ratio) VALUES ('id', 'name', 'desc', 1, 'A', 2.2, 3.3, 4.4, 'PT', 5, 6.6)",
        "INSERT INTO current_transformers (" +
            "mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, terminal_mrid, " +
            "current_transformer_info_mrid, core_burden) VALUES ('id', 'name', 'desc', 1, 'l_id', 2, true, false, 't_id', 'cti_id', 3)",
        "INSERT INTO potential_transformers (" +
            "mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, terminal_mrid, " +
            "potential_transformer_info_mrid, type) VALUES ('id', 'name', 'desc', 1, 'l_id', 2, true, false, 't_id', 'pti_id', 'T')"
    )

    override fun validate(statement: Statement) {
        ensureIndexes(
            statement,
            "current_transformer_info_mrid",
            "current_transformer_info_name",
            "potential_transformer_info_mrid",
            "potential_transformer_info_name",
            "current_transformers_mrid",
            "current_transformers_name",
            "potential_transformers_mrid",
            "potential_transformers_name"
        )
        validateRows(statement, "SELECT * FROM current_transformer_info", { rs ->
            assertThat(rs.getString("mrid"), equalTo("id"))
            assertThat(rs.getString("name"), equalTo("name"))
            assertThat(rs.getString("description"), equalTo("desc"))
            assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
            assertThat(rs.getString("accuracy_class"), equalTo("A"))
            assertThat(rs.getDouble("accuracy_limit"), equalTo(2.2))
            assertThat(rs.getInt("core_count"), equalTo(3))
            assertThat(rs.getString("ct_class"), equalTo("CT"))
            assertThat(rs.getInt("knee_point_voltage"), equalTo(4))
            assertThat(rs.getDouble("max_ratio_denominator"), equalTo(5.5))
            assertThat(rs.getDouble("max_ratio_numerator"), equalTo(6.6))
            assertThat(rs.getDouble("nominal_ratio_denominator"), equalTo(7.7))
            assertThat(rs.getDouble("nominal_ratio_numerator"), equalTo(8.8))
            assertThat(rs.getDouble("primary_ratio"), equalTo(9.9))
            assertThat(rs.getInt("rated_current"), equalTo(10))
            assertThat(rs.getInt("secondary_fls_rating"), equalTo(11))
            assertThat(rs.getDouble("secondary_ratio"), equalTo(12.12))
            assertThat(rs.getString("usage"), equalTo("U"))
        })
        validateRows(statement, "SELECT * FROM potential_transformer_info", { rs ->
            assertThat(rs.getString("mrid"), equalTo("id"))
            assertThat(rs.getString("name"), equalTo("name"))
            assertThat(rs.getString("description"), equalTo("desc"))
            assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
            assertThat(rs.getString("accuracy_class"), equalTo("A"))
            assertThat(rs.getDouble("nominal_ratio_denominator"), equalTo(2.2))
            assertThat(rs.getDouble("nominal_ratio_numerator"), equalTo(3.3))
            assertThat(rs.getDouble("primary_ratio"), equalTo(4.4))
            assertThat(rs.getString("pt_class"), equalTo("PT"))
            assertThat(rs.getInt("rated_voltage"), equalTo(5))
            assertThat(rs.getDouble("secondary_ratio"), equalTo(6.6))
        })
        validateRows(statement, "SELECT * FROM current_transformers", { rs ->
            assertThat(rs.getString("mrid"), equalTo("id"))
            assertThat(rs.getString("name"), equalTo("name"))
            assertThat(rs.getString("description"), equalTo("desc"))
            assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
            assertThat(rs.getString("location_mrid"), equalTo("l_id"))
            assertThat(rs.getInt("num_controls"), equalTo(2))
            assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
            assertThat(rs.getBoolean("in_service"), equalTo(false))
            assertThat(rs.getString("terminal_mrid"), equalTo("t_id"))
            assertThat(rs.getString("current_transformer_info_mrid"), equalTo("cti_id"))
            assertThat(rs.getInt("core_burden"), equalTo(3))
        })
        validateRows(statement, "SELECT * FROM potential_transformers", { rs ->
            assertThat(rs.getString("mrid"), equalTo("id"))
            assertThat(rs.getString("name"), equalTo("name"))
            assertThat(rs.getString("description"), equalTo("desc"))
            assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
            assertThat(rs.getString("location_mrid"), equalTo("l_id"))
            assertThat(rs.getInt("num_controls"), equalTo(2))
            assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
            assertThat(rs.getBoolean("in_service"), equalTo(false))
            assertThat(rs.getString("terminal_mrid"), equalTo("t_id"))
            assertThat(rs.getString("potential_transformer_info_mrid"), equalTo("pti_id"))
            assertThat(rs.getString("type"), equalTo("T"))
        })
    }

    override fun tearDownStatements(): List<String> = listOf(
        "DELETE FROM current_transformer_info",
        "DELETE FROM potential_transformer_info",
        "DELETE FROM current_transformers",
        "DELETE FROM potential_transformers"
    )

}
