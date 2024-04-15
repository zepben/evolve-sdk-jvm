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

object ChangeSet49Validator : ChangeSetValidator {

    override fun setUpStatements(): List<String> = listOf(
        "INSERT INTO reclose_delays (current_relay_info_mrid, reclose_delay, sequence_number) VALUES ('id1', 1.1, 2)",
        "INSERT INTO current_relay_info (mrid, name, description, num_diagram_objects, curve_setting) VALUES ('id1', 'name', 'desc', 1, 'curve')",
        "INSERT INTO current_relays (mrid, name, description, num_diagram_objects, location_mrid, num_controls, relay_delay_time, protection_kind, " +
            "directable, power_direction, current_limit_1, inverse_time_flag, time_delay_1, current_relay_info_mrid) VALUES ('id2', 'name', 'desc', 1, " +
            "'loc', 2, 3.3, 'JG', true, 'BOTH', 4.4, true, 5.5, 'id1')",
        "INSERT INTO fuses (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, " +
            "base_voltage_mrid, normal_open, open, rated_current, switch_info_mrid) VALUES ('id3', 'name', 'desc', 1, 'loc', 2, true, true, '1970-01-01', " +
            "'bvid', 3, 4, 5, 'siid')",
        "INSERT INTO tap_changer_controls (mrid, name, description, num_diagram_objects, location_mrid, num_controls, discrete, mode, monitored_phase, " +
            "target_deadband, target_value, enabled, max_allowed_target_value, min_allowed_target_value, terminal_mrid, limit_voltage, " +
            "line_drop_compensation, line_drop_r, line_drop_x, reverse_line_drop_r, reverse_line_drop_x, forward_ldc_blocking, time_delay, " +
            "co_generation_enabled) VALUES ('id4', 'name', 'desc', 1, 'loc', 2, true, 'voltage', 'ABC', 3.3, 4.4, true, 5.5, 6.6, 'tid', 7, true, 8.8, " +
            "9.9, 10.10, 11.11, true, 12.12, true)"
    )

    override fun populateStatements(): List<String> = listOf(
        "INSERT INTO reclose_delays (relay_info_mrid, reclose_delay, sequence_number) VALUES ('id5', 1.1, 2)",
        "INSERT INTO relay_info (mrid, name, description, num_diagram_objects, curve_setting, reclose_fast) VALUES ('id5', 'name', 'desc', 1, 'curve', true)",
        "INSERT INTO current_relays (mrid, name, description, num_diagram_objects, location_mrid, num_controls, model, reclosing, relay_delay_time, " +
            "protection_kind, directable, power_direction, relay_info_mrid, current_limit_1, inverse_time_flag, time_delay_1) VALUES ('id6', 'name', 'desc', " +
            "1, 'loc', 2, 'model', true, 3.3, 'ZL', true, 'BOTH', 'id5', 4.4, true, 5.5)",
        "INSERT INTO distance_relays (mrid, name, description, num_diagram_objects, location_mrid, num_controls, model, reclosing, relay_delay_time, " +
            "protection_kind, directable, power_direction, relay_info_mrid, backward_blind, backward_reach, backward_reactance, forward_blind, " +
            "forward_reach, forward_reactance, operation_phase_angle1, operation_phase_angle2, operation_phase_angle3) VALUES ('id7', 'name', 'desc', 1, " +
            "'loc', 2, 'model', true, 3.3, 'ZL', true, 'BOTH', 'id5', 4.4, 5.5, 6.6, 7.7, 8.8, 9.9, 10.10, 11.11, 12.12)",
        "INSERT INTO protection_relay_schemes (mrid, name, description, num_diagram_objects, system_mrid) VALUES ('id8', 'name', 'desc', 1, 'id9')",
        "INSERT INTO protection_relay_systems (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, " +
            "commissioned_date, protection_kind) VALUES ('id9', 'name', 'desc', 1, 'loc', 2, true, true, '1970-01-01', 'MULTI_FUNCTION')",
        "INSERT INTO voltage_relays (mrid, name, description, num_diagram_objects, location_mrid, num_controls, model, reclosing, relay_delay_time, " +
            "protection_kind, directable, power_direction, relay_info_mrid) VALUES ('id10', 'name', 'desc', 1, 'loc', 2, 'model', true, 3.3, 'ZL', true, " +
            "'BOTH', 'id5')",
        "INSERT INTO fuses (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, " +
            "base_voltage_mrid, normal_open, open, rated_current, switch_info_mrid, function_mrid) VALUES ('id11', 'name', 'desc', 1, 'loc', 2, true, true, " +
            "'1970-01-01', 'bvid', 3, 4, 5, 'siid', 'id10')",
        "INSERT INTO grounds (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, " +
            "base_voltage_mrid) VALUES ('id12', 'name', 'desc', 1, 'loc', 2, true, true, '1970-01-01', 'bvid')",
        "INSERT INTO ground_disconnectors (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, " +
            "commissioned_date, base_voltage_mrid, normal_open, open, rated_current, switch_info_mrid) VALUES ('id13', 'name', 'desc', 1, 'loc', 2, true, " +
            "true, '1970-01-01', 'bvid', 3, 4, 5, 'siid')",
        "INSERT INTO series_compensators (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, " +
            "commissioned_date, base_voltage_mrid, r, r0, x, x0, varistor_rated_current, varistor_voltage_threshold) VALUES ('id14', 'name', 'desc', 1, " +
            "'loc', 2, true, true, '1970-01-01', 'bvid', 3.3, 4.4, 5.5, 6.6, 7, 8)",
        "INSERT INTO tap_changer_controls (mrid, name, description, num_diagram_objects, location_mrid, num_controls, discrete, mode, monitored_phase, " +
            "target_deadband, target_value, enabled, max_allowed_target_value, min_allowed_target_value, rated_current, terminal_mrid, limit_voltage, " +
            "line_drop_compensation, line_drop_r, line_drop_x, reverse_line_drop_r, reverse_line_drop_x, forward_ldc_blocking, time_delay, " +
            "co_generation_enabled) VALUES ('id15', 'name', 'desc', 1, 'loc', 2, true, 'voltage', 'ABC', 3.3, 4.4, true, 5.5, 6.6, 7.7, 'tid', 8, true, 9.9, " +
            "10.10, 11.11, 12.12, true, 13.13, true)",
        "INSERT INTO protection_relay_function_thresholds (protection_relay_function_mrid, sequence_number, unit_symbol, value, name) VALUES ('id7', 1, " +
            "'METRES', 2.2, 'name')",
        "INSERT INTO protection_relay_function_time_limits (protection_relay_function_mrid, sequence_number, time_limit) VALUES ('id7', 1, 2.2)",
        "INSERT INTO protection_relay_functions_protected_switches (protection_relay_function_mrid, protected_switch_mrid) VALUES ('id7', 'psid')",
        "INSERT INTO protection_relay_functions_sensors (protection_relay_function_mrid, sensor_mrid) VALUES ('id7', 'sid')",
        "INSERT INTO protection_relay_schemes_protection_relay_functions (protection_relay_scheme_mrid, protection_relay_function_mrid) VALUES ('id8', 'id7')"
    )

    override fun validate(statement: Statement) {
        ensureIndexes(
            statement,
            "distance_relays_mrid",
            "distance_relays_name",
            "protection_relay_schemes_mrid",
            "protection_relay_schemes_name",
            "protection_relay_systems_mrid",
            "protection_relay_systems_name",
            "voltage_relays_mrid",
            "voltage_relays_name",
            "grounds_mrid",
            "grounds_name",
            "ground_disconnectors_mrid",
            "ground_disconnectors_name",
            "series_compensators_mrid",
            "series_compensators_name",
            "protection_relay_function_thresholds_protection_relay_function_mrid_sequence_number",
            "protection_relay_function_thresholds_protection_relay_function_mrid",
            "protection_relay_function_time_limits_protection_relay_function_mrid_sequence_number",
            "protection_relay_function_time_limits_protection_relay_function_mrid",
            "protection_relay_functions_protected_switches_protection_relay_function_mrid_protected_switch_mrid",
            "protection_relay_functions_protected_switches_protection_relay_function_mrid",
            "protection_relay_functions_protected_switches_protected_switch_mrid",
            "protection_relay_functions_sensors_protection_relay_function_mrid_sensor_mrid",
            "protection_relay_functions_sensors_protection_relay_function_mrid",
            "protection_relay_functions_sensors_sensor_mrid",
            "protection_relay_schemes_protection_relay_functions_protection_relay_scheme_mrid_protection_relay_function_mrid",
            "protection_relay_schemes_protection_relay_functions_protection_relay_scheme_mrid",
            "protection_relay_schemes_protection_relay_functions_protection_relay_function_mrid",
            "reclose_delays_relay_info_mrid_sequence_number",
            "reclose_delays_relay_info_mrid",
            "relay_info_mrid",
            "relay_info_name"
        )
        validateRows(statement, "SELECT * FROM reclose_delays",
            { rs ->
                assertThat(rs.getString("relay_info_mrid"), equalTo("id1"))
                assertThat(rs.getDouble("reclose_delay"), equalTo(1.1))
                assertThat(rs.getInt("sequence_number"), equalTo(2))
            },
            { rs ->
                assertThat(rs.getString("relay_info_mrid"), equalTo("id5"))
                assertThat(rs.getDouble("reclose_delay"), equalTo(1.1))
                assertThat(rs.getInt("sequence_number"), equalTo(2))
            }
        )
        validateRows(statement, "SELECT * FROM relay_info",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("curve_setting"), equalTo("curve"))
                assertThat("reclose_fast should default to NULL", rs.apply { getBoolean("reclose_fast") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id5"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("curve_setting"), equalTo("curve"))
                assertThat(rs.getBoolean("reclose_fast"), equalTo(true))
            }
        )
        validateRows(statement, "SELECT * FROM current_relays",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat("model should default to NULL", rs.apply { getString("model") }.wasNull())
                assertThat("reclosing should default to NULL", rs.apply { getBoolean("reclosing") }.wasNull())
                assertThat(rs.getDouble("relay_delay_time"), equalTo(3.3))
                assertThat(rs.getString("protection_kind"), equalTo("JG"))
                assertThat(rs.getBoolean("directable"), equalTo(true))
                assertThat(rs.getString("power_direction"), equalTo("BOTH"))
                assertThat(rs.getString("relay_info_mrid"), equalTo("id1"))
                assertThat(rs.getDouble("current_limit_1"), equalTo(4.4))
                assertThat(rs.getBoolean("inverse_time_flag"), equalTo(true))
                assertThat(rs.getDouble("time_delay_1"), equalTo(5.5))
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id6"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getString("model"), equalTo("model"))
                assertThat(rs.getBoolean("reclosing"), equalTo(true))
                assertThat(rs.getDouble("relay_delay_time"), equalTo(3.3))
                assertThat(rs.getString("protection_kind"), equalTo("ZL"))
                assertThat(rs.getBoolean("directable"), equalTo(true))
                assertThat(rs.getString("power_direction"), equalTo("BOTH"))
                assertThat(rs.getString("relay_info_mrid"), equalTo("id5"))
                assertThat(rs.getDouble("current_limit_1"), equalTo(4.4))
                assertThat(rs.getBoolean("inverse_time_flag"), equalTo(true))
                assertThat(rs.getDouble("time_delay_1"), equalTo(5.5))
            }
        )
        validateRows(
            statement, "SELECT * FROM distance_relays",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id7"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getString("model"), equalTo("model"))
                assertThat(rs.getBoolean("reclosing"), equalTo(true))
                assertThat(rs.getDouble("relay_delay_time"), equalTo(3.3))
                assertThat(rs.getString("protection_kind"), equalTo("ZL"))
                assertThat(rs.getBoolean("directable"), equalTo(true))
                assertThat(rs.getString("power_direction"), equalTo("BOTH"))
                assertThat(rs.getString("relay_info_mrid"), equalTo("id5"))
                assertThat(rs.getDouble("backward_blind"), equalTo(4.4))
                assertThat(rs.getDouble("backward_reach"), equalTo(5.5))
                assertThat(rs.getDouble("backward_reactance"), equalTo(6.6))
                assertThat(rs.getDouble("forward_blind"), equalTo(7.7))
                assertThat(rs.getDouble("forward_reach"), equalTo(8.8))
                assertThat(rs.getDouble("forward_reactance"), equalTo(9.9))
                assertThat(rs.getDouble("operation_phase_angle1"), equalTo(10.10))
                assertThat(rs.getDouble("operation_phase_angle2"), equalTo(11.11))
                assertThat(rs.getDouble("operation_phase_angle3"), equalTo(12.12))
            },
        )
        validateRows(statement, "SELECT * FROM protection_relay_schemes",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id8"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("system_mrid"), equalTo("id9"))
            }
        )
        validateRows(statement, "SELECT * FROM protection_relay_systems",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id9"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("commissioned_date"), equalTo("1970-01-01"))
                assertThat(rs.getString("protection_kind"), equalTo("MULTI_FUNCTION"))
            }
        )
        validateRows(statement, "SELECT * FROM voltage_relays",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id10"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getString("model"), equalTo("model"))
                assertThat(rs.getBoolean("reclosing"), equalTo(true))
                assertThat(rs.getDouble("relay_delay_time"), equalTo(3.3))
                assertThat(rs.getString("protection_kind"), equalTo("ZL"))
                assertThat(rs.getBoolean("directable"), equalTo(true))
                assertThat(rs.getString("power_direction"), equalTo("BOTH"))
                assertThat(rs.getString("relay_info_mrid"), equalTo("id5"))
            }
        )
        validateRows(statement, "SELECT * FROM fuses",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id3"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("commissioned_date"), equalTo("1970-01-01"))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bvid"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getInt("rated_current"), equalTo(5))
                assertThat(rs.getString("switch_info_mrid"), equalTo("siid"))
                assertThat("function_mrid should default to NULL", rs.apply { getString("function_mrid") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id11"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("commissioned_date"), equalTo("1970-01-01"))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bvid"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getInt("rated_current"), equalTo(5))
                assertThat(rs.getString("switch_info_mrid"), equalTo("siid"))
                assertThat(rs.getString("function_mrid"), equalTo("id10"))
            }
        )
        validateRows(statement, "SELECT * FROM grounds",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id12"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("commissioned_date"), equalTo("1970-01-01"))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bvid"))
            }
        )
        validateRows(statement, "SELECT * FROM ground_disconnectors",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id13"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("commissioned_date"), equalTo("1970-01-01"))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bvid"))
                assertThat(rs.getInt("normal_open"), equalTo(3))
                assertThat(rs.getInt("open"), equalTo(4))
                assertThat(rs.getInt("rated_current"), equalTo(5))
                assertThat(rs.getString("switch_info_mrid"), equalTo("siid"))
            }
        )
        validateRows(statement, "SELECT * FROM series_compensators",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id14"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("commissioned_date"), equalTo("1970-01-01"))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bvid"))
                assertThat(rs.getDouble("r"), equalTo(3.3))
                assertThat(rs.getDouble("r0"), equalTo(4.4))
                assertThat(rs.getDouble("x"), equalTo(5.5))
                assertThat(rs.getDouble("x0"), equalTo(6.6))
                assertThat(rs.getInt("varistor_rated_current"), equalTo(7))
                assertThat(rs.getInt("varistor_voltage_threshold"), equalTo(8))
            }
        )
        validateRows(statement, "SELECT * FROM tap_changer_controls",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id4"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("discrete"), equalTo(true))
                assertThat(rs.getString("mode"), equalTo("voltage"))
                assertThat(rs.getString("monitored_phase"), equalTo("ABC"))
                assertThat(rs.getDouble("target_deadband"), equalTo(3.3))
                assertThat(rs.getDouble("target_value"), equalTo(4.4))
                assertThat(rs.getBoolean("enabled"), equalTo(true))
                assertThat(rs.getDouble("max_allowed_target_value"), equalTo(5.5))
                assertThat(rs.getDouble("min_allowed_target_value"), equalTo(6.6))
                assertThat("rated_current should default to NULL", rs.apply { getDouble("rated_current") }.wasNull())
                assertThat(rs.getString("terminal_mrid"), equalTo("tid"))
                assertThat(rs.getInt("limit_voltage"), equalTo(7))
                assertThat(rs.getBoolean("line_drop_compensation"), equalTo(true))
                assertThat(rs.getDouble("line_drop_r"), equalTo(8.8))
                assertThat(rs.getDouble("line_drop_x"), equalTo(9.9))
                assertThat(rs.getDouble("reverse_line_drop_r"), equalTo(10.10))
                assertThat(rs.getDouble("reverse_line_drop_x"), equalTo(11.11))
                assertThat(rs.getBoolean("forward_ldc_blocking"), equalTo(true))
                assertThat(rs.getDouble("time_delay"), equalTo(12.12))
                assertThat(rs.getBoolean("co_generation_enabled"), equalTo(true))
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id15"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("loc"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("discrete"), equalTo(true))
                assertThat(rs.getString("mode"), equalTo("voltage"))
                assertThat(rs.getString("monitored_phase"), equalTo("ABC"))
                assertThat(rs.getDouble("target_deadband"), equalTo(3.3))
                assertThat(rs.getDouble("target_value"), equalTo(4.4))
                assertThat(rs.getBoolean("enabled"), equalTo(true))
                assertThat(rs.getDouble("max_allowed_target_value"), equalTo(5.5))
                assertThat(rs.getDouble("min_allowed_target_value"), equalTo(6.6))
                assertThat(rs.getDouble("rated_current"), equalTo(7.7))
                assertThat(rs.getString("terminal_mrid"), equalTo("tid"))
                assertThat(rs.getInt("limit_voltage"), equalTo(8))
                assertThat(rs.getBoolean("line_drop_compensation"), equalTo(true))
                assertThat(rs.getDouble("line_drop_r"), equalTo(9.9))
                assertThat(rs.getDouble("line_drop_x"), equalTo(10.10))
                assertThat(rs.getDouble("reverse_line_drop_r"), equalTo(11.11))
                assertThat(rs.getDouble("reverse_line_drop_x"), equalTo(12.12))
                assertThat(rs.getBoolean("forward_ldc_blocking"), equalTo(true))
                assertThat(rs.getDouble("time_delay"), equalTo(13.13))
                assertThat(rs.getBoolean("co_generation_enabled"), equalTo(true))
            }
        )
        validateRows(statement, "SELECT * FROM protection_relay_function_thresholds",
            { rs ->
                assertThat(rs.getString("protection_relay_function_mrid"), equalTo("id7"))
                assertThat(rs.getInt("sequence_number"), equalTo(1))
                assertThat(rs.getString("unit_symbol"), equalTo("METRES"))
                assertThat(rs.getDouble("value"), equalTo(2.2))
                assertThat(rs.getString("name"), equalTo("name"))
            }
        )
        validateRows(statement, "SELECT * FROM protection_relay_function_time_limits",
            { rs ->
                assertThat(rs.getString("protection_relay_function_mrid"), equalTo("id7"))
                assertThat(rs.getInt("sequence_number"), equalTo(1))
                assertThat(rs.getDouble("time_limit"), equalTo(2.2))
            }
        )
        validateRows(statement, "SELECT * FROM protection_relay_functions_protected_switches",
            { rs ->
                assertThat(rs.getString("protection_relay_function_mrid"), equalTo("id7"))
                assertThat(rs.getString("protected_switch_mrid"), equalTo("psid"))
            }
        )
        validateRows(statement, "SELECT * FROM protection_relay_functions_sensors",
            { rs ->
                assertThat(rs.getString("protection_relay_function_mrid"), equalTo("id7"))
                assertThat(rs.getString("sensor_mrid"), equalTo("sid"))
            }
        )
        validateRows(statement, "SELECT * FROM protection_relay_schemes_protection_relay_functions",
            { rs ->
                assertThat(rs.getString("protection_relay_scheme_mrid"), equalTo("id8"))
                assertThat(rs.getString("protection_relay_function_mrid"), equalTo("id7"))
            }
        )
    }

    override fun tearDownStatements(): List<String> = listOf(
        "DELETE FROM reclose_delays",
        "DELETE FROM relay_info",
        "DELETE FROM current_relays",
        "DELETE FROM distance_relays",
        "DELETE FROM protection_relay_schemes",
        "DELETE FROM protection_relay_systems",
        "DELETE FROM voltage_relays",
        "DELETE FROM fuses",
        "DELETE FROM grounds",
        "DELETE FROM ground_disconnectors",
        "DELETE FROM series_compensators",
        "DELETE FROM tap_changer_controls",
        "DELETE FROM protection_relay_function_thresholds",
        "DELETE FROM protection_relay_function_time_limits",
        "DELETE FROM protection_relay_functions_protected_switches",
        "DELETE FROM protection_relay_functions_sensors",
        "DELETE FROM protection_relay_schemes_protection_relay_functions",
    )

}
