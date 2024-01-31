/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.upgrade.changesets

import com.zepben.evolve.database.sqlite.upgrade.ChangeSet

internal fun changeSet49() = ChangeSet(
    49,
    sql
)

private val sql = listOf(
    """CREATE TABLE distance_relays (
        mrid TEXT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        num_diagram_objects INTEGER NOT NULL,
        location_mrid TEXT NULL,
        num_controls INTEGER NOT NULL,
        model TEXT NULL,
        reclosing BOOLEAN NULL,
        relay_delay_time NUMBER NULL,
        protection_kind TEXT NOT NULL,
        directable BOOLEAN NULL,
        power_direction TEXT NOT NULL,
        backward_blind NUMBER NULL,
        backward_reach NUMBER NULL,
        backward_reactance NUMBER NULL,
        forward_blind NUMBER NULL,
        forward_reach NUMBER NULL,
        forward_reactance NUMBER NULL,
        operation_phase_angle1 NUMBER NULL,
        operation_phase_angle2 NUMBER NULL,
        operation_phase_angle3 NUMBER NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX distance_relays_mrid ON distance_relays (mrid);",
    "CREATE INDEX distance_relays_name ON distance_relays (name);",

    """CREATE TABLE protection_relay_schemes (
        mrid TEXT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        num_diagram_objects INTEGER NOT NULL,
        system_mrid TEXT NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX protection_relay_schemes_mrid ON protection_relay_schemes (mrid);",
    "CREATE INDEX protection_relay_schemes_name ON protection_relay_schemes (name);",

    """CREATE TABLE protection_relay_systems (
        mrid TEXT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        num_diagram_objects INTEGER NOT NULL,
        location_mrid TEXT NULL,
        num_controls INTEGER NOT NULL,
        normally_in_service BOOLEAN,
        in_service BOOLEAN,
        commissioned_date TEXT NULL,
        protection_kind TEXT NOT NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX protection_relay_systems_mrid ON protection_relay_systems (mrid);",
    "CREATE INDEX protection_relay_systems_name ON protection_relay_systems (name);",

    """CREATE TABLE voltage_relays (
        mrid TEXT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        num_diagram_objects INTEGER NOT NULL,
        location_mrid TEXT NULL,
        num_controls INTEGER NOT NULL,
        model TEXT NULL,
        reclosing BOOLEAN NULL,
        relay_delay_time NUMBER NULL,
        protection_kind TEXT NOT NULL,
        directable BOOLEAN NULL,
        power_direction TEXT NOT NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX voltage_relays_mrid ON voltage_relays (mrid);",
    "CREATE INDEX voltage_relays_name ON voltage_relays (name);",

    """CREATE TABLE grounds (
        mrid TEXT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        num_diagram_objects INTEGER NOT NULL,
        location_mrid TEXT NULL,
        num_controls INTEGER NOT NULL,
        normally_in_service BOOLEAN,
        in_service BOOLEAN,
        commissioned_date TEXT NULL,
        base_voltage_mrid TEXT NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX grounds_mrid ON grounds (mrid);",
    "CREATE INDEX grounds_name ON grounds (name);",

    """CREATE TABLE ground_disconnectors (
        mrid TEXT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        num_diagram_objects INTEGER NOT NULL,
        location_mrid TEXT NULL,
        num_controls INTEGER NOT NULL,
        normally_in_service BOOLEAN,
        in_service BOOLEAN,
        commissioned_date TEXT NULL,
        base_voltage_mrid TEXT NULL,
        normal_open INTEGER NOT NULL,
        open INTEGER NOT NULL,
        rated_current INTEGER NULL,
        switch_info_mrid TEXT NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX ground_disconnectors_mrid ON ground_disconnectors (mrid);",
    "CREATE INDEX ground_disconnectors_name ON ground_disconnectors (name);",

    """CREATE TABLE series_compensators (
        mrid TEXT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        num_diagram_objects INTEGER NOT NULL,
        location_mrid TEXT NULL,
        num_controls INTEGER NOT NULL,
        normally_in_service BOOLEAN,
        in_service BOOLEAN,
        commissioned_date TEXT NULL,
        base_voltage_mrid TEXT NULL,
        r NUMBER NULL,
        r0 NUMBER NULL,
        x NUMBER NULL,
        x0 NUMBER NULL,
        varistor_rated_current INTEGER NULL,
        varistor_voltage_threshold INTEGER NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX series_compensators_mrid ON series_compensators (mrid);",
    "CREATE INDEX series_compensators_name ON series_compensators (name);",
    
    """CREATE TABLE protection_relay_function_thresholds (
        protection_relay_function_mrid TEXT NOT NULL,
        sequence_number INTEGER NOT NULL,
        unit_symbol TEXT NOT NULL,
        value NUMBER NOT NULL,
        name TEXT NULL
    );""".trimIndent(),
    """CREATE UNIQUE INDEX protection_relay_function_thresholds_protection_relay_function_mrid_sequence_number
        ON protection_relay_function_thresholds (protection_relay_function_mrid, sequence_number);""".trimIndent(),
    """CREATE INDEX protection_relay_function_thresholds_protection_relay_function_mrid
        ON protection_relay_function_thresholds (protection_relay_function_mrid);""".trimIndent(),

    """CREATE TABLE protection_relay_function_time_limits (
        protection_relay_function_mrid TEXT NOT NULL,
        sequence_number INTEGER NOT NULL,
        time_limit NUMBER NOT NULL
    );""".trimIndent(),
    """CREATE UNIQUE INDEX protection_relay_function_time_limits_protection_relay_function_mrid_sequence_number
        ON protection_relay_function_time_limits (protection_relay_function_mrid, sequence_number);""".trimIndent(),
    """CREATE INDEX protection_relay_function_time_limits_protection_relay_function_mrid
        ON protection_relay_function_time_limits (protection_relay_function_mrid);""".trimIndent(),
    
    """CREATE TABLE protection_relay_functions_protected_switches (
        protection_relay_function_mrid TEXT NOT NULL,
        protected_switch_mrid TEXT NOT NULL
    );""".trimIndent(),
    """CREATE UNIQUE INDEX protection_relay_functions_protected_switches_protection_relay_function_mrid_protected_switch_mrid
        ON protection_relay_functions_protected_switches (protection_relay_function_mrid, protected_switch_mrid);""".trimIndent(),
    """CREATE INDEX protection_relay_functions_protected_switches_protection_relay_function_mrid
        ON protection_relay_functions_protected_switches (protection_relay_function_mrid);""".trimIndent(),
    """CREATE INDEX protection_relay_functions_protected_switches_protected_switch_mrid
        ON protection_relay_functions_protected_switches (protected_switch_mrid);""".trimIndent(),

    """CREATE TABLE protection_relay_functions_sensors (
        protection_relay_function_mrid TEXT NOT NULL,
        sensor_mrid TEXT NOT NULL
    );""".trimIndent(),
    """CREATE UNIQUE INDEX protection_relay_functions_sensors_protection_relay_function_mrid_sensor_mrid
        ON protection_relay_functions_sensors (protection_relay_function_mrid, sensor_mrid);""".trimIndent(),
    """CREATE INDEX protection_relay_functions_sensors_protection_relay_function_mrid
        ON protection_relay_functions_sensors (protection_relay_function_mrid);""".trimIndent(),
    """CREATE INDEX protection_relay_functions_sensors_sensor_mrid
        ON protection_relay_functions_sensors (sensor_mrid);""".trimIndent(),

    """CREATE TABLE protection_relay_schemes_protection_relay_functions (
        protection_relay_scheme_mrid TEXT NOT NULL,
        protection_relay_function_mrid TEXT NOT NULL
    );""".trimIndent(),
    """CREATE UNIQUE INDEX protection_relay_schemes_protection_relay_functions_protection_relay_scheme_mrid_protection_relay_function_mrid
        ON protection_relay_schemes_protection_relay_functions (protection_relay_scheme_mrid, protection_relay_function_mrid);""".trimMargin(),
    """CREATE INDEX protection_relay_schemes_protection_relay_functions_protection_relay_scheme_mrid
        ON protection_relay_schemes_protection_relay_functions (protection_relay_scheme_mrid);""".trimIndent(),
    """CREATE INDEX protection_relay_schemes_protection_relay_functions_protection_relay_function_mrid
        ON protection_relay_schemes_protection_relay_functions (protection_relay_function_mrid);""".trimIndent(),

    "CREATE TABLE reclose_delays_new (relay_info_mrid TEXT NOT NULL, reclose_delay NUMBER NOT NULL, sequence_number INTEGER NOT NULL);",
    "CREATE UNIQUE INDEX reclose_delays_relay_info_mrid_sequence_number ON reclose_delays_new (relay_info_mrid, sequence_number);",
    "CREATE INDEX reclose_delays_relay_info_mrid ON reclose_delays_new (relay_info_mrid);",
    "INSERT INTO reclose_delays_new SELECT current_relay_info_mrid, reclose_delay, sequence_number FROM reclose_delays;",
    "DROP TABLE reclose_delays;",
    "ALTER TABLE reclose_delays_new RENAME TO reclose_delays;",

    """CREATE TABLE relay_info (
        mrid TEXT NOT NULL,
        name TEXT NOT NULL,
        description TEXT NOT NULL,
        num_diagram_objects INTEGER NOT NULL,
        curve_setting TEXT NULL
    );""".trimIndent(),
    "CREATE UNIQUE INDEX relay_info_mrid ON relay_info (mrid);",
    "CREATE INDEX relay_info_name ON relay_info (name);",
    "INSERT INTO relay_info SELECT mrid, name, description, num_diagram_objects, curve_setting FROM current_relay_info;",
    "DROP TABLE current_relay_info;",

    "ALTER TABLE current_relays ADD model TEXT NULL;",
    "ALTER TABLE current_relays ADD reclosing BOOLEAN NULL;",
    "ALTER TABLE fuses ADD function_mrid TEXT NULL;",
    "ALTER TABLE tap_changer_controls ADD rated_current NUMBER NULL;",

    "DROP TABLE protection_equipment_protected_switches;"
)
