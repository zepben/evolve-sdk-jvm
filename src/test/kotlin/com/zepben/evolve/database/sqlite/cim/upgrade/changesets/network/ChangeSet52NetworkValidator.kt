/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets.network

import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.ChangeSetValidator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import java.sql.ResultSet
import java.sql.Statement

object ChangeSet52NetworkValidator : ChangeSetValidator {

    //
    // NOTE: In the validators we are only checking the columns that were actually changed.
    //

    override fun setUpStatements(): List<String> = listOf(
        "INSERT INTO battery_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p, battery_state, rated_e, stored_e) " +
            "VALUES ('id1', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1, 1, 'battery_state', 1, 1);",
        "INSERT INTO ev_charging_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p) " +
            "VALUES ('id1', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1, 1);",
        "INSERT INTO photo_voltaic_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p) " +
            "VALUES ('id1', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1, 1);",
        "INSERT INTO power_electronics_wind_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p) " +
            "VALUES ('id1', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1, 1);",
        "INSERT INTO streetlights (mrid, name, description, num_diagram_objects, location_mrid, pole_mrid, lamp_kind, light_rating) " +
            "VALUES ('id1', 'name', 'description', 1, 'location_mrid', 'pole_mrid', 'lamp_kind', 1);",
        "INSERT INTO tap_changer_controls (mrid, name, description, num_diagram_objects, location_mrid, num_controls, discrete, mode, monitored_phase, target_deadband, target_value, enabled, max_allowed_target_value, min_allowed_target_value, rated_current, terminal_mrid, limit_voltage, line_drop_compensation, line_drop_r, line_drop_x, reverse_line_drop_r, reverse_line_drop_x, forward_ldc_blocking, time_delay, co_generation_enabled) " +
            "VALUES ('id1', 'name', 'description', 1, 'location_mrid', 1, true, 'mode', 'monitored_phase', 1, 1, true, 1, 1, 1, 'terminal_mrid', 1, true, 1, 1, 1, 1, true, 1, true);",

        "INSERT INTO linear_shunt_compensators (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, base_voltage_mrid, control_enabled, regulating_control_mrid, shunt_compensator_info_mrid, grounded, nom_u, phase_connection, sections, b0_per_section, b_per_section, g0_per_section, g_per_section) " +
            "VALUES ('id1', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', true, 'regulating_control_mrid', 'shunt_compensator_info_mrid', true, 1, 'phase_connection', 1, 1, 1, 1, 1);",
        "INSERT INTO power_transformer_end_ratings (power_transformer_end_mrid, cooling_type, rated_s) " +
            "VALUES ('id1', 'cooling_type', 1);",

        "INSERT INTO power_electronics_connections (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, base_voltage_mrid, control_enabled, regulating_control_mrid, max_i_fault, max_q, min_q, p, q, rated_s, rated_u, inverter_standard, sustain_op_overvolt_limit, stop_at_over_freq, stop_at_under_freq, inv_volt_watt_resp_mode, inv_watt_resp_v1, inv_watt_resp_v2, inv_watt_resp_v3, inv_watt_resp_v4, inv_watt_resp_p_at_v1, inv_watt_resp_p_at_v2, inv_watt_resp_p_at_v3, inv_watt_resp_p_at_v4, inv_volt_var_resp_mode, inv_var_resp_v1, inv_var_resp_v2, inv_var_resp_v3, inv_var_resp_v4, inv_var_resp_q_at_v1, inv_var_resp_q_at_v2, inv_var_resp_q_at_v3, inv_var_resp_q_at_v4, inv_reactive_power_mode, inv_fix_reactive_power) " +
            "VALUES ('id1', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', true, 'regulating_control_mrid', 1, 1, 1, 1, 1, 1, 1, 'inverter_standard', 1, 1, 1, true, 1, 1, 1, 1, 1, 1, 1, 1, true, 1, 1, 1, 1, 1, 1, 1, 1, true, 1);",
    )

    override fun populateStatements(): List<String> = listOf(
        "INSERT INTO battery_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p, battery_state, rated_e, stored_e) " +
            "VALUES ('id2', 'name', 'description', 2, 'location_mrid', 2, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 2, 2, 'battery_state', 2, 2);",
        "INSERT INTO ev_charging_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p) " +
            "VALUES ('id2', 'name', 'description', 2, 'location_mrid', 2, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 2, 2);",
        "INSERT INTO photo_voltaic_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p) " +
            "VALUES ('id2', 'name', 'description', 2, 'location_mrid', 2, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 2, 2);",
        "INSERT INTO power_electronics_wind_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p) " +
            "VALUES ('id2', 'name', 'description', 2, 'location_mrid', 2, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 2, 2);",
        "INSERT INTO streetlights (mrid, name, description, num_diagram_objects, location_mrid, pole_mrid, lamp_kind, light_rating) " +
            "VALUES ('id2', 'name', 'description', 2, 'location_mrid', 'pole_mrid', 'lamp_kind', 2);",
        "INSERT INTO tap_changer_controls (mrid, name, description, num_diagram_objects, location_mrid, num_controls, discrete, mode, monitored_phase, target_deadband, target_value, enabled, max_allowed_target_value, min_allowed_target_value, rated_current, terminal_mrid, limit_voltage, line_drop_compensation, line_drop_r, line_drop_x, reverse_line_drop_r, reverse_line_drop_x, forward_ldc_blocking, time_delay, co_generation_enabled) " +
            "VALUES ('id2', 'name', 'description', 2, 'location_mrid', 2, true, 'mode', 'monitored_phase', 2, 2, true, 2, 2, 2, 'terminal_mrid', 2, true, 2, 2, 2, 2, true, 2, true);",

        "INSERT INTO linear_shunt_compensators (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, base_voltage_mrid, control_enabled, regulating_control_mrid, shunt_compensator_info_mrid, grounded, nom_u, phase_connection, sections, b0_per_section, b_per_section, g0_per_section, g_per_section) " +
            "VALUES ('id2', 'name', 'description', 2, 'location_mrid', 2, true, true, 'commissioned_date', 'base_voltage_mrid', true, 'regulating_control_mrid', 'shunt_compensator_info_mrid', true, 2, 'phase_connection', 2, 2, 2, 2, 2);",
        "INSERT INTO power_transformer_end_ratings (power_transformer_end_mrid, cooling_type, rated_s) " +
            "VALUES ('id2', 'cooling_type', 2);",

        "INSERT INTO power_electronics_connections (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, base_voltage_mrid, control_enabled, regulating_control_mrid, max_i_fault, max_q, min_q, p, q, rated_s, rated_u, inverter_standard, sustain_op_overvolt_limit, stop_at_over_freq, stop_at_under_freq, inv_volt_watt_resp_mode, inv_watt_resp_v2, inv_watt_resp_v2, inv_watt_resp_v3, inv_watt_resp_v4, inv_watt_resp_p_at_v2, inv_watt_resp_p_at_v2, inv_watt_resp_p_at_v3, inv_watt_resp_p_at_v4, inv_volt_var_resp_mode, inv_var_resp_v1, inv_var_resp_v2, inv_var_resp_v3, inv_var_resp_v4, inv_var_resp_q_at_v1, inv_var_resp_q_at_v2, inv_var_resp_q_at_v3, inv_var_resp_q_at_v4, inv_reactive_power_mode, inv_fix_reactive_power) " +
            "VALUES ('id2', 'name', 'description', 2, 'location_mrid', 2, true, true, 'commissioned_date', 'base_voltage_mrid', true, 'regulating_control_mrid', 2, 2, 2, 2, 2, 2, 2, 'inverter_standard', 2, 2, 2, true, 2, 2, 2, 2, 2, 2, 2, 2, true, 2, 2, 2, 2, 2, 2, 2, 2, true, 2);",
    )

    override fun validate(statement: Statement) {
        // Ensure new indexes were added.
        ensureIndexes(statement, "location_street_addresses_location_mrid_address_field")

        // Ensure column types were updated correctly.
        validateBatteryUnits(statement)
        validateEvChargingUnits(statement)
        validatePhotoVoltaicUnits(statement)
        validatePowerElectronicsWindUnits(statement)
        validateStreetlights(statement)
        validateTapChangerControls(statement)

        // Ensure column nullability were updated correctly.
        validateLinearShuntCompensators(statement)
        validatePowerTransformerEndRatings(statement)

        // Ensure column types and nullability were updated correctly.
        validatePowerElectronicsConnections(statement)
    }

    override fun tearDownStatements(): List<String> =
        listOf(
            "DELETE FROM battery_units;",
            "DELETE FROM ev_charging_units;",
            "DELETE FROM photo_voltaic_units;",
            "DELETE FROM power_electronics_wind_units;",
            "DELETE FROM streetlights;",
            "DELETE FROM tap_changer_controls;",

            "DELETE FROM linear_shunt_compensators;",
            "DELETE FROM power_transformer_end_ratings;",

            "DELETE FROM power_electronics_connections;",
        )

    private fun validateStreetlights(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(statement, "streetlights_mrid", "streetlights_name")

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM streetlights",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getInt("light_rating"), equalTo(1))
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getInt("light_rating"), equalTo(2))
            }
        )
    }

    private fun validatePowerTransformerEndRatings(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(
            statement,
            "power_transformer_end_ratings_power_transformer_end_mrid_cooling_type",
            "power_transformer_end_ratings_power_transformer_end_mrid"
        )

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM power_transformer_end_ratings",
            { rs ->
                assertThat(rs.getString("power_transformer_end_mrid"), equalTo("id1"))
            },
            { rs ->
                assertThat(rs.getString("power_transformer_end_mrid"), equalTo("id2"))
            }
        )
    }

    private fun validateTapChangerControls(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(statement, "tap_changer_controls_mrid", "tap_changer_controls_name")

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM tap_changer_controls",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getInt("limit_voltage"), equalTo(1))
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getInt("limit_voltage"), equalTo(2))
            }
        )
    }

    private fun validateLinearShuntCompensators(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(statement, "linear_shunt_compensators_mrid", "linear_shunt_compensators_name")

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM linear_shunt_compensators",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                validateRegulatingCondEqFields(rs)
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                validateRegulatingCondEqFields(rs)
            }
        )
    }

    private fun validatePowerElectronicsConnections(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(statement, "power_electronics_connections_mrid", "power_electronics_connections_name")

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM power_electronics_connections",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getInt("max_i_fault"), equalTo(1))
                assertThat(rs.getInt("rated_s"), equalTo(1))
                assertThat(rs.getInt("rated_u"), equalTo(1))
                assertThat(rs.getInt("inv_var_resp_v1"), equalTo(1))
                assertThat(rs.getInt("inv_var_resp_v2"), equalTo(1))
                assertThat(rs.getInt("inv_var_resp_v3"), equalTo(1))
                assertThat(rs.getInt("inv_var_resp_v4"), equalTo(1))

                validateRegulatingCondEqFields(rs)
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getInt("max_i_fault"), equalTo(2))
                assertThat(rs.getInt("rated_s"), equalTo(2))
                assertThat(rs.getInt("rated_u"), equalTo(2))
                assertThat(rs.getInt("inv_var_resp_v1"), equalTo(2))
                assertThat(rs.getInt("inv_var_resp_v2"), equalTo(2))
                assertThat(rs.getInt("inv_var_resp_v3"), equalTo(2))
                assertThat(rs.getInt("inv_var_resp_v4"), equalTo(2))

                validateRegulatingCondEqFields(rs)
            },
        )
    }

    private fun validateBatteryUnits(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(statement, "battery_units_mrid", "battery_units_name", "battery_units_power_electronics_connection_mrid")

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM battery_units",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                validatePowerElectronicsUnitFields(rs, 1)
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                validatePowerElectronicsUnitFields(rs, 2)
            }
        )

    }

    private fun validateEvChargingUnits(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(statement, "ev_charging_units_mrid", "ev_charging_units_name", "ev_charging_units_power_electronics_connection_mrid")

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM ev_charging_units",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                validatePowerElectronicsUnitFields(rs, 1)
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                validatePowerElectronicsUnitFields(rs, 2)
            }
        )

    }

    private fun validatePhotoVoltaicUnits(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(statement, "photo_voltaic_units_mrid", "photo_voltaic_units_name", "photo_voltaic_units_power_electronics_connection_mrid")

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM photo_voltaic_units",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                validatePowerElectronicsUnitFields(rs, 1)
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                validatePowerElectronicsUnitFields(rs, 2)
            }
        )

    }

    private fun validatePowerElectronicsWindUnits(statement: Statement) {
        // Make sure the indexes were recreated.
        ensureIndexes(
            statement,
            "power_electronics_wind_units_mrid",
            "power_electronics_wind_units_name",
            "power_electronics_wind_units_power_electronics_connection_mrid"
        )

        // Make sure the data was copied.
        validateRows(
            statement,
            "SELECT * FROM power_electronics_wind_units",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                validatePowerElectronicsUnitFields(rs, 1)
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                validatePowerElectronicsUnitFields(rs, 2)
            }
        )

    }

    private fun validateRegulatingCondEqFields(rs: ResultSet) {
        assertThat("control should be enabled", rs.getBoolean("control_enabled"))
    }

    private fun validatePowerElectronicsUnitFields(rs: ResultSet, value: Int) {
        assertThat(rs.getInt("max_p"), equalTo(value))
        assertThat(rs.getInt("min_p"), equalTo(value))
    }

}
