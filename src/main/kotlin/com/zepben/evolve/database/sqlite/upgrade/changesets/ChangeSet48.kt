/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.upgrade.changesets

import com.zepben.evolve.database.sqlite.upgrade.ChangeSet

internal fun changeSet48() = ChangeSet(
    48,
    sql
)

private val sql = listOf(
    """CREATE TABLE tap_changer_controls (
        mrid TEXT NOT NULL, 
        name TEXT NOT NULL, 
        description TEXT NOT NULL, 
        num_diagram_objects INTEGER NOT NULL, 
        location_mrid TEXT NULL, 
        num_controls INTEGER NOT NULL, 
        discrete BOOLEAN NULL, 
        mode TEXT NOT NULL, 
        monitored_phase TEXT NOT NULL, 
        target_deadband NUMBER NULL, 
        target_value NUMBER NULL, 
        enabled BOOLEAN NULL, 
        max_allowed_target_value NUMBER NULL, 
        min_allowed_target_value NUMBER NULL, 
        terminal_mrid TEXT NULL, 
        limit_voltage NUMBER NULL, 
        line_drop_compensation BOOLEAN NULL, 
        line_drop_r NUMBER NULL, 
        line_drop_x NUMBER NULL, 
        reverse_line_drop_r NUMBER NULL, 
        reverse_line_drop_x NUMBER NULL, 
        forward_ldc_blocking BOOLEAN NULL, 
        time_delay NUMBER NULL, 
        co_generation_enabled BOOLEAN NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX tap_changer_controls_mrid ON tap_changer_controls (mrid);",
    "CREATE INDEX tap_changer_controls_name ON tap_changer_controls (name);",

    """CREATE TABLE ev_charging_units (
       mrid TEXT NOT NULL, 
       name TEXT NOT NULL, 
       description TEXT NOT NULL, 
       num_diagram_objects INTEGER NOT NULL, 
       location_mrid TEXT NULL, 
       num_controls INTEGER NOT NULL, 
       normally_in_service BOOLEAN, 
       in_service BOOLEAN, 
       commissioned_date TEXT NULL, 
       power_electronics_connection_mrid TEXT NULL, 
       max_p NUMBER NULL, 
       min_p NUMBER NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX ev_charging_units_mrid ON ev_charging_units (mrid);",
    "CREATE INDEX ev_charging_units_name ON ev_charging_units (name);",
    "CREATE INDEX ev_charging_units_power_electronics_connection_mrid ON ev_charging_units (power_electronics_connection_mrid);",

    """CREATE TABLE reclose_delays (
        current_relay_info_mrid TEXT NOT NULL, 
        reclose_delay NUMBER NOT NULL, 
        sequence_number INTEGER NOT NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX reclose_delays_current_relay_info_mrid_sequence_number ON reclose_delays (current_relay_info_mrid, sequence_number);",
    "CREATE INDEX reclose_delays_current_relay_info_mrid ON reclose_delays (current_relay_info_mrid);",

    """CREATE TABLE power_transformer_end_ratings (
        power_transformer_end_mrid TEXT NOT NULL, 
        cooling_type TEXT NOT NULL, 
        rated_s INTEGER NOT NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX power_transformer_end_ratings_power_transformer_end_mrid_cooling_type ON power_transformer_end_ratings (power_transformer_end_mrid, cooling_type);",
    "CREATE INDEX power_transformer_end_ratings_power_transformer_end_mrid ON power_transformer_end_ratings (power_transformer_end_mrid);",

    "ALTER TABLE usage_points ADD rated_power INTEGER NULL;",
    "ALTER TABLE usage_points ADD approved_inverter_capacity INTEGER NULL;",
    "ALTER TABLE current_relays ADD directable BOOLEAN NULL;",
    "ALTER TABLE current_relays ADD power_direction TEXT NOT NULL DEFAULT 'UNKNOWN_DIRECTION';",

    // Update all equipment tables with commissioned_date
    "ALTER TABLE ac_line_segments ADD commissioned_date TEXT NULL;",
    "ALTER TABLE power_transformers ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE linear_shunt_compensators ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE energy_consumers ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE energy_sources ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE junctions ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE busbar_sections ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE disconnectors ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE jumpers ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE fuses ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE load_break_switches ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE breakers ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE reclosers ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE equivalent_branches ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE photo_voltaic_unit ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE power_electronics_wind_unit ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE battery_unit ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE current_relays ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE fault_indicators ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE current_transformers ADD COLUMN commissioned_date TEXT NULL;",
    "ALTER TABLE potential_transformers ADD COLUMN commissioned_date TEXT NULL;",
    // PEC columns
    "ALTER TABLE power_electronics_connection ADD COLUMN inverter_standard TEXT NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN sustain_op_overvolt_limit INTEGER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN stop_at_over_freq NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN stop_at_under_freq NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_volt_watt_resp_mode BOOLEAN NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_watt_resp_v1 INTEGER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_watt_resp_v2 INTEGER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_watt_resp_v3 INTEGER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_watt_resp_v4 INTEGER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_watt_resp_p_at_v1 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_watt_resp_p_at_v2 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_watt_resp_p_at_v3 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_watt_resp_p_at_v4 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_volt_var_resp_mode BOOLEAN NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_var_resp_v1 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_var_resp_v2 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_var_resp_v3 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_var_resp_v4 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_var_resp_q_at_v1 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_var_resp_q_at_v2 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_var_resp_q_at_v3 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_var_resp_q_at_v4 NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_reactive_power_mode BOOLEAN NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN inv_fix_reactive_power NUMBER NULL;",
    "ALTER TABLE power_electronics_connection ADD COLUMN regulating_control_mrid;",
    "ALTER TABLE linear_shunt_compensators ADD COLUMN regulating_control_mrid;",
    "ALTER TABLE ratio_tap_changers ADD COLUMN tap_changer_control_mrid;",
    // Translate rated_s to new table.
    """
    insert into power_transformer_end_ratings (power_transformer_end_mrid, cooling_type, rated_s)
    SELECT mrid, "UNKNOWN_COOLING_TYPE", rated_s FROM power_transformer_ends where rated_s IS NOT NULL;
    """.trimIndent(),
    "ALTER TABLE power_transformer_ends DROP COLUMN rated_s;",

    )
