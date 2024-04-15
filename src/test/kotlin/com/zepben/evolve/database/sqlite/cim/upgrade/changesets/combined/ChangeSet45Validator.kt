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
import org.hamcrest.Matchers.nullValue
import java.sql.Statement

object ChangeSet45Validator : ChangeSetValidator {
    override fun setUpStatements(): List<String> = listOf(
        """
        INSERT INTO breakers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, base_voltage_mrid,
                              normal_open, open)
        VALUES ('b0', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4)
        """.trimIndent(),
        """
        INSERT INTO disconnectors (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service,
                                   base_voltage_mrid, normal_open, open)
        VALUES ('dc0', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4)
        """.trimIndent(),
        """
        INSERT INTO fuses (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, base_voltage_mrid,
                           normal_open, open)
        VALUES ('fuse0', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4)
        """.trimIndent(),
        """
        INSERT INTO jumpers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, base_voltage_mrid,
                             normal_open, open)
        VALUES ('jmp0', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4)
        """.trimIndent(),
        """
        INSERT INTO load_break_switches (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service,
                                         base_voltage_mrid, normal_open, open)
        VALUES ('lbs0', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4)
        """.trimIndent(),
        """
        INSERT INTO reclosers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, base_voltage_mrid,
                               normal_open, open)
        VALUES ('rc0', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4)
        """.trimIndent()
    )

    override fun populateStatements(): List<String> = listOf(
        """
        INSERT INTO breakers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, base_voltage_mrid,
                              normal_open, open, switch_info_mrid, rated_current, breaking_capacity, in_transit_time)
        VALUES ('b1', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4, 'swi', 5, 6, 7.7)
        """.trimIndent(),
        """
        INSERT INTO disconnectors (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service,
                                   base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current)
        VALUES ('dc1', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4, 'swi', 5)
        """.trimIndent(),
        """
        INSERT INTO fuses (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, base_voltage_mrid,
                           normal_open, open, switch_info_mrid, rated_current)
        VALUES ('fuse1', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4, 'swi', 5)
        """.trimIndent(),
        """
        INSERT INTO jumpers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, base_voltage_mrid,
                             normal_open, open, switch_info_mrid, rated_current)
        VALUES ('jmp1', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4, 'swi', 5)
        """.trimIndent(),
        """
        INSERT INTO load_break_switches (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service,
                                         base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, breaking_capacity)
        VALUES ('lbs1', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4, 'swi', 5, 6)
        """.trimIndent(),
        """
        INSERT INTO reclosers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, base_voltage_mrid,
                               normal_open, open, switch_info_mrid, rated_current, breaking_capacity)
        VALUES ('rc1', 'name', 'desc', 1, 'loc', 2, true, false, 'bv', 3, 4, 'swi', 5, 6)
        """.trimIndent(),
        """
        INSERT INTO switch_info (mrid, name, description, num_diagram_objects, rated_interrupting_time)
        VALUES ('swi', 'name', 'desc', 1, 2.2)
        """.trimIndent(),
        """
        INSERT INTO current_relay_info (mrid, name, description, num_diagram_objects, curve_setting)
        VALUES ('cri', 'name', 'desc', 1, 'cs')
        """.trimIndent(),
        """
        INSERT INTO current_relays (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service,
                                    relay_delay_time, protection_kind, current_limit_1, inverse_time_flag, time_delay_1, current_relay_info_mrid)
        VALUES ('cr', 'name', 'desc', 1, 'loc', 2, true, false, 3.3, 'SEF', 4.4, true, 5.5, 'cri')
        """.trimIndent(),
        """
        INSERT INTO protection_equipment_protected_switches (protection_equipment_mrid, protected_switch_mrid)
        VALUES ('cr', 'b1')
        """.trimIndent()
    )

    override fun validate(statement: Statement) {
        ensureIndexes(
            statement,
            "switch_info_mrid",
            "switch_info_name",
            "current_relay_info_mrid",
            "current_relay_info_name",
            "current_relays_mrid",
            "current_relays_name",
            "protection_equipment_protected_switches_protection_equipment_mrid",
            "protection_equipment_protected_switches_protected_switch_mrid"
        )
        validateRows(statement, "SELECT * FROM breakers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("b0"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), nullValue())
                assertThat("rated_current should default to NULL", rs.apply { getInt("rated_current") }.wasNull())
                assertThat("breaking_capacity should default to NULL", rs.apply { getInt("breaking_capacity") }.wasNull())
                assertThat("in_transit_time should default to NULL", rs.apply { getDouble("in_transit_time") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("b1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(5))
                assertThat(rs.getInt("breaking_capacity"), equalTo(6))
                assertThat(rs.getDouble("in_transit_time"), equalTo(7.7))
            }
        )
        validateRows(statement, "SELECT * FROM disconnectors",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("dc0"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), nullValue())
                assertThat("rated_current should default to NULL", rs.apply { getInt("rated_current") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("dc1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(5))
            }
        )
        validateRows(statement, "SELECT * FROM fuses",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("fuse0"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), nullValue())
                assertThat("rated_current should default to NULL", rs.apply { getInt("rated_current") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("fuse1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(5))
            }
        )
        validateRows(statement, "SELECT * FROM jumpers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("jmp0"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), nullValue())
                assertThat("rated_current should default to NULL", rs.apply { getInt("rated_current") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("jmp1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(5))
            }
        )
        validateRows(statement, "SELECT * FROM load_break_switches",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("lbs0"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), nullValue())
                assertThat("rated_current should default to NULL", rs.apply { getInt("rated_current") }.wasNull())
                assertThat("breaking_capacity should default to NULL", rs.apply { getInt("breaking_capacity") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("lbs1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(5))
                assertThat(rs.getInt("breaking_capacity"), equalTo(6))
            }
        )
        validateRows(statement, "SELECT * FROM reclosers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("rc0"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), nullValue())
                assertThat("rated_current should default to NULL", rs.apply { getInt("rated_current") }.wasNull())
                assertThat("breaking_capacity should default to NULL", rs.apply { getInt("breaking_capacity") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("rc1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(5))
                assertThat(rs.getInt("breaking_capacity"), equalTo(6))
            }
        )
    }

    override fun tearDownStatements(): List<String> = listOf(
        "DELETE FROM breakers",
        "DELETE FROM disconnectors",
        "DELETE FROM fuses",
        "DELETE FROM jumpers",
        "DELETE FROM load_break_switches",
        "DELETE FROM reclosers",
        "DELETE FROM switch_info",
        "DELETE FROM current_relay_info",
        "DELETE FROM current_relays",
        "DELETE FROM protection_equipment_protected_switches"
    )

}
