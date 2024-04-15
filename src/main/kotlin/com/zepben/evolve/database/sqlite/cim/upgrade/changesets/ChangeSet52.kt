/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets

import com.zepben.evolve.database.sqlite.cim.upgrade.Change
import com.zepben.evolve.database.sqlite.cim.upgrade.ChangeSet
import com.zepben.evolve.database.paths.DatabaseType

internal fun changeSet52() = ChangeSet(
    52,
    listOf(
        // Network Changes

        `Add missing index for location_street_addresses`,
        `Rename columns in power_transformer_ends`,

        `Change column types in battery_units`,
        `Change column types in ev_charging_units`,
        `Change column types in photo_voltaic_units`,
        `Change column types in power_electronics_wind_units`,
        `Change column types in streetlights`,
        `Change column types in tap_changer_controls`,

        `Change column nullability in linear_shunt_compensators`,
        `Change column nullability in power_transformer_end_ratings`,

        `Change column types and nullability in power_electronics_connections`,

        // Diagram Changes

        `Change column types in diagram_object_points`,
    )
)

// ###################
// # Network Changes #
// ###################

@Suppress("ObjectPropertyName")
private val `Add missing index for location_street_addresses` = Change(
    listOf(
        "CREATE UNIQUE INDEX location_street_addresses_location_mrid_address_field ON location_street_addresses (location_mrid, address_field);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Rename columns in power_transformer_ends` = Change(
    listOf(
        "ALTER TABLE power_transformer_ends RENAME COLUMN R to r;",
        "ALTER TABLE power_transformer_ends RENAME COLUMN R0 to r0;",
        "ALTER TABLE power_transformer_ends RENAME COLUMN X to x;",
        "ALTER TABLE power_transformer_ends RENAME COLUMN X0 to x0;",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column types in battery_units` = Change(
    listOf(
        "CREATE TABLE battery_units2 (mrid TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, num_diagram_objects INTEGER NOT NULL, location_mrid TEXT NULL, num_controls INTEGER NOT NULL, normally_in_service BOOLEAN, in_service BOOLEAN, commissioned_date TEXT NULL, power_electronics_connection_mrid TEXT NULL, max_p INTEGER NULL, min_p INTEGER NULL, battery_state TEXT NOT NULL, rated_e INTEGER NULL, stored_e INTEGER NULL);",
        "INSERT INTO battery_units2 SELECT mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p, battery_state, rated_e, stored_e FROM battery_units;",
        "DROP TABLE battery_units;",
        "ALTER TABLE battery_units2 RENAME TO battery_units;",
        "CREATE UNIQUE INDEX battery_units_mrid ON battery_units (mrid);",
        "CREATE INDEX battery_units_name ON battery_units (name);",
        "CREATE INDEX battery_units_power_electronics_connection_mrid ON battery_units (power_electronics_connection_mrid);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column types in ev_charging_units` = Change(
    listOf(
        "CREATE TABLE ev_charging_units2 (mrid TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, num_diagram_objects INTEGER NOT NULL, location_mrid TEXT NULL, num_controls INTEGER NOT NULL, normally_in_service BOOLEAN, in_service BOOLEAN, commissioned_date TEXT NULL, power_electronics_connection_mrid TEXT NULL, max_p INTEGER NULL, min_p INTEGER NULL);",
        "INSERT INTO ev_charging_units2 SELECT mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p FROM ev_charging_units;",
        "DROP TABLE ev_charging_units;",
        "ALTER TABLE ev_charging_units2 RENAME TO ev_charging_units;",
        "CREATE UNIQUE INDEX ev_charging_units_mrid ON ev_charging_units (mrid);",
        "CREATE INDEX ev_charging_units_name ON ev_charging_units (name);",
        "CREATE INDEX ev_charging_units_power_electronics_connection_mrid ON ev_charging_units (power_electronics_connection_mrid);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column types in photo_voltaic_units` = Change(
    listOf(
        "CREATE TABLE photo_voltaic_units2 (mrid TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, num_diagram_objects INTEGER NOT NULL, location_mrid TEXT NULL, num_controls INTEGER NOT NULL, normally_in_service BOOLEAN, in_service BOOLEAN, commissioned_date TEXT NULL, power_electronics_connection_mrid TEXT NULL, max_p INTEGER NULL, min_p INTEGER NULL);",
        "INSERT INTO photo_voltaic_units2 SELECT mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p FROM photo_voltaic_units;",
        "DROP TABLE photo_voltaic_units;",
        "ALTER TABLE photo_voltaic_units2 RENAME TO photo_voltaic_units;",
        "CREATE UNIQUE INDEX photo_voltaic_units_mrid ON photo_voltaic_units (mrid);",
        "CREATE INDEX photo_voltaic_units_name ON photo_voltaic_units (name);",
        "CREATE INDEX photo_voltaic_units_power_electronics_connection_mrid ON photo_voltaic_units (power_electronics_connection_mrid);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column types in power_electronics_wind_units` = Change(
    listOf(
        "CREATE TABLE power_electronics_wind_units2 (mrid TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, num_diagram_objects INTEGER NOT NULL, location_mrid TEXT NULL, num_controls INTEGER NOT NULL, normally_in_service BOOLEAN, in_service BOOLEAN, commissioned_date TEXT NULL, power_electronics_connection_mrid TEXT NULL, max_p INTEGER NULL, min_p INTEGER NULL);",
        "INSERT INTO power_electronics_wind_units2 SELECT mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, power_electronics_connection_mrid, max_p, min_p FROM power_electronics_wind_units;",
        "DROP TABLE power_electronics_wind_units;",
        "ALTER TABLE power_electronics_wind_units2 RENAME TO power_electronics_wind_units;",
        "CREATE UNIQUE INDEX power_electronics_wind_units_mrid ON power_electronics_wind_units (mrid);",
        "CREATE INDEX power_electronics_wind_units_name ON power_electronics_wind_units (name);",
        "CREATE INDEX power_electronics_wind_units_power_electronics_connection_mrid ON power_electronics_wind_units (power_electronics_connection_mrid);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column types in streetlights` = Change(
    listOf(
        "CREATE TABLE streetlights2 (mrid TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, num_diagram_objects INTEGER NOT NULL, location_mrid TEXT NULL, pole_mrid TEXT NULL, lamp_kind TEXT NOT NULL, light_rating INTEGER NULL);",
        "INSERT INTO streetlights2 SELECT mrid, name, description, num_diagram_objects, location_mrid, pole_mrid, lamp_kind, light_rating FROM streetlights;",
        "DROP TABLE streetlights;",
        "ALTER TABLE streetlights2 RENAME TO streetlights;",
        "CREATE UNIQUE INDEX streetlights_mrid ON streetlights (mrid);",
        "CREATE INDEX streetlights_name ON streetlights (name);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column types in tap_changer_controls` = Change(
    listOf(
        "CREATE TABLE tap_changer_controls2 (mrid TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, num_diagram_objects INTEGER NOT NULL, location_mrid TEXT NULL, num_controls INTEGER NOT NULL, discrete BOOLEAN NULL, mode TEXT NOT NULL, monitored_phase TEXT NOT NULL, target_deadband NUMBER NULL, target_value NUMBER NULL, enabled BOOLEAN NULL, max_allowed_target_value NUMBER NULL, min_allowed_target_value NUMBER NULL, rated_current NUMBER NULL, terminal_mrid TEXT NULL, limit_voltage INTEGER NULL, line_drop_compensation BOOLEAN NULL, line_drop_r NUMBER NULL, line_drop_x NUMBER NULL, reverse_line_drop_r NUMBER NULL, reverse_line_drop_x NUMBER NULL, forward_ldc_blocking BOOLEAN NULL, time_delay NUMBER NULL, co_generation_enabled BOOLEAN NULL);",
        "INSERT INTO tap_changer_controls2 SELECT mrid, name, description, num_diagram_objects, location_mrid, num_controls, discrete, mode, monitored_phase, target_deadband, target_value, enabled, max_allowed_target_value, min_allowed_target_value, rated_current, terminal_mrid, limit_voltage, line_drop_compensation, line_drop_r, line_drop_x, reverse_line_drop_r, reverse_line_drop_x, forward_ldc_blocking, time_delay, co_generation_enabled FROM tap_changer_controls;",
        "DROP TABLE tap_changer_controls;",
        "ALTER TABLE tap_changer_controls2 RENAME TO tap_changer_controls;",
        "CREATE UNIQUE INDEX tap_changer_controls_mrid ON tap_changer_controls (mrid);",
        "CREATE INDEX tap_changer_controls_name ON tap_changer_controls (name);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column nullability in linear_shunt_compensators` = Change(
    listOf(
        "CREATE TABLE linear_shunt_compensators2 (mrid TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, num_diagram_objects INTEGER NOT NULL, location_mrid TEXT NULL, num_controls INTEGER NOT NULL, normally_in_service BOOLEAN, in_service BOOLEAN, commissioned_date TEXT NULL, base_voltage_mrid TEXT NULL, control_enabled BOOLEAN NOT NULL, regulating_control_mrid TEXT NULL, shunt_compensator_info_mrid TEXT NULL, grounded BOOLEAN NOT NULL, nom_u INTEGER NULL, phase_connection TEXT NOT NULL, sections NUMBER NULL, b0_per_section NUMBER NULL, b_per_section NUMBER NULL, g0_per_section NUMBER NULL, g_per_section NUMBER NULL);",
        "INSERT INTO linear_shunt_compensators2 SELECT mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, base_voltage_mrid, control_enabled, regulating_control_mrid, shunt_compensator_info_mrid, grounded, nom_u, phase_connection, sections, b0_per_section, b_per_section, g0_per_section, g_per_section FROM linear_shunt_compensators;",
        "DROP TABLE linear_shunt_compensators;",
        "ALTER TABLE linear_shunt_compensators2 RENAME TO linear_shunt_compensators;",
        "CREATE UNIQUE INDEX linear_shunt_compensators_mrid ON linear_shunt_compensators (mrid);",
        "CREATE INDEX linear_shunt_compensators_name ON linear_shunt_compensators (name);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column nullability in power_transformer_end_ratings` = Change(
    listOf(
        "CREATE TABLE power_transformer_end_ratings2 (power_transformer_end_mrid TEXT NOT NULL, cooling_type TEXT NOT NULL, rated_s INTEGER NOT NULL);",
        "INSERT INTO power_transformer_end_ratings2 SELECT power_transformer_end_mrid, cooling_type, rated_s FROM power_transformer_end_ratings;",
        "DROP TABLE power_transformer_end_ratings;",
        "ALTER TABLE power_transformer_end_ratings2 RENAME TO power_transformer_end_ratings;",
        "CREATE UNIQUE INDEX power_transformer_end_ratings_power_transformer_end_mrid_cooling_type ON power_transformer_end_ratings (power_transformer_end_mrid, cooling_type);",
        "CREATE INDEX power_transformer_end_ratings_power_transformer_end_mrid ON power_transformer_end_ratings (power_transformer_end_mrid);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Change column types and nullability in power_electronics_connections` = Change(
    listOf(
        "CREATE TABLE power_electronics_connections2 (mrid TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, num_diagram_objects INTEGER NOT NULL, location_mrid TEXT NULL, num_controls INTEGER NOT NULL, normally_in_service BOOLEAN, in_service BOOLEAN, commissioned_date TEXT NULL, base_voltage_mrid TEXT NULL, control_enabled BOOLEAN NOT NULL, regulating_control_mrid TEXT NULL, max_i_fault INTEGER NULL, max_q NUMBER NULL, min_q NUMBER NULL, p NUMBER NULL, q NUMBER NULL, rated_s INTEGER NULL, rated_u INTEGER NULL, inverter_standard TEXT NULL, sustain_op_overvolt_limit INTEGER NULL, stop_at_over_freq NUMBER NULL, stop_at_under_freq NUMBER NULL, inv_volt_watt_resp_mode BOOLEAN NULL, inv_watt_resp_v1 INTEGER NULL, inv_watt_resp_v2 INTEGER NULL, inv_watt_resp_v3 INTEGER NULL, inv_watt_resp_v4 INTEGER NULL, inv_watt_resp_p_at_v1 NUMBER NULL, inv_watt_resp_p_at_v2 NUMBER NULL, inv_watt_resp_p_at_v3 NUMBER NULL, inv_watt_resp_p_at_v4 NUMBER NULL, inv_volt_var_resp_mode BOOLEAN NULL, inv_var_resp_v1 INTEGER NULL, inv_var_resp_v2 INTEGER NULL, inv_var_resp_v3 INTEGER NULL, inv_var_resp_v4 INTEGER NULL, inv_var_resp_q_at_v1 NUMBER NULL, inv_var_resp_q_at_v2 NUMBER NULL, inv_var_resp_q_at_v3 NUMBER NULL, inv_var_resp_q_at_v4 NUMBER NULL, inv_reactive_power_mode BOOLEAN NULL, inv_fix_reactive_power NUMBER NULL);",
        "INSERT INTO power_electronics_connections2 SELECT mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, commissioned_date, base_voltage_mrid, control_enabled, regulating_control_mrid, max_i_fault, max_q, min_q, p, q, rated_s, rated_u, inverter_standard, sustain_op_overvolt_limit, stop_at_over_freq, stop_at_under_freq, inv_volt_watt_resp_mode, inv_watt_resp_v1, inv_watt_resp_v2, inv_watt_resp_v3, inv_watt_resp_v4, inv_watt_resp_p_at_v1, inv_watt_resp_p_at_v2, inv_watt_resp_p_at_v3, inv_watt_resp_p_at_v4, inv_volt_var_resp_mode, inv_var_resp_v1, inv_var_resp_v2, inv_var_resp_v3, inv_var_resp_v4, inv_var_resp_q_at_v1, inv_var_resp_q_at_v2, inv_var_resp_q_at_v3, inv_var_resp_q_at_v4, inv_reactive_power_mode, inv_fix_reactive_power FROM power_electronics_connections;",
        "DROP TABLE power_electronics_connections;",
        "ALTER TABLE power_electronics_connections2 RENAME TO power_electronics_connections;",
        "CREATE UNIQUE INDEX power_electronics_connections_mrid ON power_electronics_connections (mrid);",
        "CREATE INDEX power_electronics_connections_name ON power_electronics_connections (name);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

// ###################
// # Diagram Changes #
// ###################

@Suppress("ObjectPropertyName")
private val `Change column types in diagram_object_points` = Change(
    listOf(
        "CREATE TABLE diagram_object_points2 (diagram_object_mrid TEXT NOT NULL, sequence_number INTEGER NOT NULL, x_position NUMBER NULL, y_position NUMBER NULL);",
        "INSERT INTO diagram_object_points2 SELECT diagram_object_mrid, sequence_number, x_position, y_position FROM diagram_object_points;",
        "DROP TABLE diagram_object_points;",
        "ALTER TABLE diagram_object_points2 RENAME TO diagram_object_points;",
        "CREATE UNIQUE INDEX diagram_object_points_diagram_object_mrid_sequence_number ON diagram_object_points (diagram_object_mrid, sequence_number);",
        "CREATE INDEX diagram_object_points_diagram_object_mrid ON diagram_object_points (diagram_object_mrid);",
    ),
    targetDatabases = setOf(DatabaseType.DIAGRAM)
)
