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

internal fun changeSet51() = ChangeSet(
    51,
    listOf(
        `Rename battery_unit to battery_units`,
        `Rename photo_voltaic_unit to photo_voltaic_units`,
        `Rename power_electronics_connection to power_electronics_connections`,
        `Rename power_electronics_connection_phase to power_electronics_connection_phases`,
        `Rename power_electronics_wind_unit to power_electronics_wind_units`,
        `Rename transformer_star_impedance to transformer_star_impedances`,
    )
)

@Suppress("ObjectPropertyName")
private val `Rename battery_unit to battery_units` = Change(
    listOf(
        "DROP INDEX battery_unit_mrid;",
        "DROP INDEX battery_unit_name;",
        "DROP INDEX battery_unit_power_electronics_connection_mrid;",

        "ALTER TABLE battery_unit RENAME TO battery_units;",

        "CREATE UNIQUE INDEX battery_units_mrid ON battery_units (mrid);",
        "CREATE INDEX battery_units_name ON battery_units (name);",
        "CREATE INDEX battery_units_power_electronics_connection_mrid ON battery_units (power_electronics_connection_mrid);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Rename photo_voltaic_unit to photo_voltaic_units` = Change(
    listOf(
        "DROP INDEX photo_voltaic_unit_mrid;",
        "DROP INDEX photo_voltaic_unit_name;",
        "DROP INDEX photo_voltaic_unit_power_electronics_connection_mrid;",

        "ALTER TABLE photo_voltaic_unit RENAME TO photo_voltaic_units;",

        "CREATE UNIQUE INDEX photo_voltaic_units_mrid ON photo_voltaic_units (mrid);",
        "CREATE INDEX photo_voltaic_units_name ON photo_voltaic_units (name);",
        "CREATE INDEX photo_voltaic_units_power_electronics_connection_mrid ON photo_voltaic_units (power_electronics_connection_mrid);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Rename power_electronics_connection to power_electronics_connections` = Change(
    listOf(
        "DROP INDEX power_electronics_connection_mrid;",
        "DROP INDEX power_electronics_connection_name;",

        "ALTER TABLE power_electronics_connection RENAME TO power_electronics_connections;",

        "CREATE UNIQUE INDEX power_electronics_connections_mrid ON power_electronics_connections (mrid);",
        "CREATE INDEX power_electronics_connections_name ON power_electronics_connections (name);",
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Rename power_electronics_connection_phase to power_electronics_connection_phases` = Change(
    listOf(
        "DROP INDEX power_electronics_connection_phase_mrid;",
        "DROP INDEX power_electronics_connection_phase_name;",
        "DROP INDEX power_electronics_connection_phase_power_electronics_connection_mrid;",

        "ALTER TABLE power_electronics_connection_phase RENAME TO power_electronics_connection_phases;",

        "CREATE UNIQUE INDEX power_electronics_connection_phases_mrid ON power_electronics_connection_phases (mrid);",
        "CREATE INDEX power_electronics_connection_phases_name ON power_electronics_connection_phases (name);",
        "CREATE INDEX power_electronics_connection_phases_power_electronics_connection_mrid ON power_electronics_connection_phases (power_electronics_connection_mrid);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Rename power_electronics_wind_unit to power_electronics_wind_units` = Change(
    listOf(
        "DROP INDEX power_electronics_wind_unit_mrid;",
        "DROP INDEX power_electronics_wind_unit_name;",
        "DROP INDEX power_electronics_wind_unit_power_electronics_connection_mrid;",

        "ALTER TABLE power_electronics_wind_unit RENAME TO power_electronics_wind_units;",

        "CREATE UNIQUE INDEX power_electronics_wind_units_mrid ON power_electronics_wind_units (mrid);",
        "CREATE INDEX power_electronics_wind_units_name ON power_electronics_wind_units (name);",
        "CREATE INDEX power_electronics_wind_units_power_electronics_connection_mrid ON power_electronics_wind_units (power_electronics_connection_mrid);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Rename transformer_star_impedance to transformer_star_impedances` = Change(
    listOf(
        "DROP INDEX transformer_star_impedance_mrid;",
        "DROP INDEX transformer_star_impedance_transformer_end_info_mrid;",
        "DROP INDEX transformer_star_impedance_name;",

        "ALTER TABLE transformer_star_impedance RENAME TO transformer_star_impedances;",

        "CREATE UNIQUE INDEX transformer_star_impedances_mrid ON transformer_star_impedances (mrid);",
        "CREATE UNIQUE INDEX transformer_star_impedances_transformer_end_info_mrid ON transformer_star_impedances (transformer_end_info_mrid);",
        "CREATE INDEX transformer_star_impedances_name ON transformer_star_impedances (name);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)
