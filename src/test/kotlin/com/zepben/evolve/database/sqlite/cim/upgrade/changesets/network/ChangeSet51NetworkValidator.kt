/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets.network

import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.ChangeSetValidator
import java.sql.Statement

object ChangeSet51NetworkValidator : ChangeSetValidator {

    override fun setUpStatements(): List<String> = listOf(
        "INSERT INTO battery_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p, battery_state, rated_e, stored_e) VALUES ('mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1.1, 1.1, 'battery_state', 1, 1);",
        "INSERT INTO photo_voltaic_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p) VALUES ('mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1.1, 1.1);",
        "INSERT INTO power_electronics_connection (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, base_voltage_mrid, control_enabled, regulating_control_mrid, max_i_fault, max_q, min_q, p, q, rated_s, rated_u, inverter_standard, sustain_op_overvolt_limit, stop_at_over_freq, stop_at_under_freq, inv_volt_watt_resp_mode, inv_watt_resp_v1, inv_watt_resp_v2, inv_watt_resp_v3, inv_watt_resp_v4, inv_watt_resp_p_at_v1, inv_watt_resp_p_at_v2, inv_watt_resp_p_at_v3, inv_watt_resp_p_at_v4, inv_volt_var_resp_mode, inv_var_resp_v1, inv_var_resp_v2, inv_var_resp_v3, inv_var_resp_v4, inv_var_resp_q_at_v1, inv_var_resp_q_at_v2, inv_var_resp_q_at_v3, inv_var_resp_q_at_v4, inv_reactive_power_mode, inv_fix_reactive_power) VALUES ('mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', true, 'regulating_control_mrid', 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 'inverter_standard', 1, 1.1, 1.1, true, 1, 1, 1, 1, 1.1, 1.1, 1.1, 1.1, true, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, true, 1.1);",
        "INSERT INTO power_electronics_connection_phase (mrid, name, description, num_diagram_objects, location_mrid, num_controls, power_electronics_connection_mrid, p, phase, q) VALUES ('mrid', 'name', 'description', 1, 'location_mrid', 1, 'power_electronics_connection_mrid', 1.1, 'phase', 1.1);",
        "INSERT INTO power_electronics_wind_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p) VALUES ('mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1.1, 1.1);",
        "INSERT INTO transformer_star_impedance (mrid, name, description, num_diagram_objects, R, R0, X, X0, transformer_end_info_mrid) VALUES ('mrid', 'name', 'description', 1, 1.1, 1.1, 1.1, 1.1, 'transformer_end_info_mrid');",
    )

    // We do not need to populate anything as we are not changing any of the table structures, just renaming them.
    override fun populateStatements(): List<String> = emptyList()

    override fun validate(statement: Statement) {
        ensureNewAdded(statement)
        ensureOldRemoved(statement)
    }

    override fun tearDownStatements(): List<String> =
        listOf(
            "DELETE FROM battery_units;",
            "DELETE FROM photo_voltaic_units;",
            "DELETE FROM power_electronics_connections;",
            "DELETE FROM power_electronics_connection_phases;",
            "DELETE FROM power_electronics_wind_units;",
            "DELETE FROM transformer_star_impedances;",
        )

    private fun ensureNewAdded(statement: Statement) {
        ensureTables(
            statement,
            "battery_units",
            "photo_voltaic_units",
            "power_electronics_connections",
            "power_electronics_connection_phases",
            "power_electronics_wind_units",
            "transformer_star_impedances",
            present = true
        )

        ensureIndexes(
            statement,
            "battery_units_mrid",
            "battery_units_name",
            "battery_units_power_electronics_connection_mrid",
            "photo_voltaic_units_mrid",
            "photo_voltaic_units_name",
            "photo_voltaic_units_power_electronics_connection_mrid",
            "power_electronics_connections_mrid",
            "power_electronics_connections_name",
            "power_electronics_connection_phases_mrid",
            "power_electronics_connection_phases_name",
            "power_electronics_connection_phases_power_electronics_connection_mrid",
            "power_electronics_wind_units_mrid",
            "power_electronics_wind_units_name",
            "power_electronics_wind_units_power_electronics_connection_mrid",
            "transformer_star_impedances_mrid",
            "transformer_star_impedances_transformer_end_info_mrid",
            "transformer_star_impedances_name",
            present = true
        )
    }

    private fun ensureOldRemoved(statement: Statement) {
        ensureTables(
            statement,
            "battery_unit",
            "photo_voltaic_unit",
            "power_electronics_connection",
            "power_electronics_connection_phase",
            "power_electronics_wind_unit",
            "transformer_star_impedance",
            present = false
        )

        ensureIndexes(
            statement,
            "battery_unit_mrid",
            "battery_unit_name",
            "battery_unit_power_electronics_connection_mrid",
            "photo_voltaic_unit_mrid",
            "photo_voltaic_unit_name",
            "photo_voltaic_unit_power_electronics_connection_mrid",
            "power_electronics_connection_mrid",
            "power_electronics_connection_name",
            "power_electronics_connection_phase_mrid",
            "power_electronics_connection_phase_name",
            "power_electronics_connection_phase_power_electronics_connection_mrid",
            "power_electronics_wind_unit_mrid",
            "power_electronics_wind_unit_name",
            "power_electronics_wind_unit_power_electronics_connection_mrid",
            "transformer_star_impedance_mrid",
            "transformer_star_impedance_transformer_end_info_mrid",
            "transformer_star_impedance_name",
            present = false
        )
    }

}
