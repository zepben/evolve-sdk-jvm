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

internal fun changeSet50() = ChangeSet(
    50,
    listOf(
        // The shared data must be cleaned first to allow correct functioning of other queries.
        `Clean customer data from tables shared with network`,
        `Clean network data from tables shared with customer`,
        `Clean organisation names from diagrams database`,

        // Clean out the names table for entries from the other services.
        `Clean customer names`,
        `Clean diagram names`,
        `Clean network names`,
        `Clean unused name types`,

        // Drop unused tables.
        `Drop customer tables`,
        `Drop diagram tables`,
        `Drop network tables`,
        `Drop tables shared between customer and network`
    )
)

@Suppress("ObjectPropertyName")
private val `Clean customer data from tables shared with network` = Change(
    listOf(
        """
        DELETE FROM
            names
        WHERE
            identified_object_mrid IN (SELECT mrid FROM organisations)
            AND identified_object_mrid NOT IN (SELECT organisation_mrid FROM asset_owners);
        """.trimIndent(),
        "DELETE FROM organisations WHERE mrid NOT IN (SELECT organisation_mrid FROM asset_owners);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Clean network data from tables shared with customer` = Change(
    listOf(
        """
        DELETE FROM
            names
        WHERE
            identified_object_mrid IN (SELECT mrid FROM organisations)
            AND identified_object_mrid NOT IN (SELECT organisation_mrid FROM customers);
        """.trimIndent(),
        "DELETE FROM organisations WHERE mrid NOT IN (SELECT organisation_mrid FROM customers);"
    ),
    targetDatabases = setOf(DatabaseType.CUSTOMER)
)

@Suppress("ObjectPropertyName")
private val `Clean organisation names from diagrams database` = Change(
    listOf(
        "DELETE FROM names WHERE identified_object_mrid IN (SELECT mrid FROM organisations);"
    ),
    targetDatabases = setOf(DatabaseType.DIAGRAM)
)

@Suppress("ObjectPropertyName")
private val `Clean customer names` = Change(
    listOf(
        """
        WITH mrids AS (
            SELECT mrid FROM customer_agreements
            UNION SELECT mrid FROM customers
            UNION SELECT mrid FROM pricing_structures
            UNION SELECT mrid FROM tariffs
        )
        DELETE FROM names WHERE identified_object_mrid IN (SELECT mrid FROM mrids);
        """.trimIndent(),
    ),
    targetDatabases = setOf(DatabaseType.DIAGRAM, DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Clean diagram names` = Change(
    listOf(
        """
        WITH mrids AS (
            SELECT mrid FROM diagrams
            UNION SELECT mrid FROM diagram_objects
        )
        DELETE FROM names WHERE identified_object_mrid IN (SELECT mrid FROM mrids);
        """.trimIndent(),
    ),
    targetDatabases = setOf(DatabaseType.CUSTOMER, DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Clean network names` = Change(
    listOf(
        """
        WITH mrids AS (
            SELECT mrid FROM ac_line_segments
            UNION SELECT mrid FROM accumulators
            UNION SELECT mrid FROM analogs
            UNION SELECT mrid FROM asset_owners
            UNION SELECT mrid FROM base_voltages
            UNION SELECT mrid FROM battery_unit
            UNION SELECT mrid FROM breakers
            UNION SELECT mrid FROM busbar_sections
            UNION SELECT mrid FROM cable_info
            UNION SELECT mrid FROM circuits
            UNION SELECT mrid FROM connectivity_nodes
            UNION SELECT mrid FROM controls
            UNION SELECT mrid FROM current_relays
            UNION SELECT mrid FROM current_transformer_info
            UNION SELECT mrid FROM current_transformers
            UNION SELECT mrid FROM disconnectors
            UNION SELECT mrid FROM discretes
            UNION SELECT mrid FROM distance_relays
            UNION SELECT mrid FROM energy_consumer_phases
            UNION SELECT mrid FROM energy_consumers
            UNION SELECT mrid FROM energy_source_phases
            UNION SELECT mrid FROM energy_sources
            UNION SELECT mrid FROM equivalent_branches
            UNION SELECT mrid FROM ev_charging_units
            UNION SELECT mrid FROM fault_indicators
            UNION SELECT mrid FROM feeders
            UNION SELECT mrid FROM fuses
            UNION SELECT mrid FROM geographical_regions
            UNION SELECT mrid FROM grounds
            UNION SELECT mrid FROM ground_disconnectors
            UNION SELECT mrid FROM jumpers
            UNION SELECT mrid FROM junctions
            UNION SELECT mrid FROM linear_shunt_compensators
            UNION SELECT mrid FROM load_break_switches
            UNION SELECT mrid FROM locations
            UNION SELECT mrid FROM loops
            UNION SELECT mrid FROM lv_feeders
            UNION SELECT mrid FROM meters
            UNION SELECT mrid FROM no_load_tests
            UNION SELECT mrid FROM open_circuit_tests
            UNION SELECT mrid FROM operational_restrictions
            UNION SELECT mrid FROM overhead_wire_info
            UNION SELECT mrid FROM per_length_sequence_impedances
            UNION SELECT mrid FROM photo_voltaic_unit
            UNION SELECT mrid FROM poles
            UNION SELECT mrid FROM potential_transformer_info
            UNION SELECT mrid FROM potential_transformers
            UNION SELECT mrid FROM power_electronics_connection
            UNION SELECT mrid FROM power_electronics_connection_phase
            UNION SELECT mrid FROM power_electronics_wind_unit
            UNION SELECT mrid FROM power_transformer_ends
            UNION SELECT mrid FROM power_transformer_info
            UNION SELECT mrid FROM power_transformers
            UNION SELECT mrid FROM protection_relay_schemes
            UNION SELECT mrid FROM protection_relay_systems
            UNION SELECT mrid FROM ratio_tap_changers
            UNION SELECT mrid FROM reclosers
            UNION SELECT mrid FROM relay_info
            UNION SELECT mrid FROM remote_controls
            UNION SELECT mrid FROM remote_sources
            UNION SELECT mrid FROM series_compensators
            UNION SELECT mrid FROM short_circuit_tests
            UNION SELECT mrid FROM shunt_compensator_info
            UNION SELECT mrid FROM sites
            UNION SELECT mrid FROM streetlights
            UNION SELECT mrid FROM sub_geographical_regions
            UNION SELECT mrid FROM substations
            UNION SELECT mrid FROM switch_info
            UNION SELECT mrid FROM tap_changer_controls
            UNION SELECT mrid FROM terminals
            UNION SELECT mrid FROM transformer_end_info
            UNION SELECT mrid FROM transformer_star_impedance
            UNION SELECT mrid FROM transformer_tank_info
            UNION SELECT mrid FROM usage_points
            UNION SELECT mrid FROM voltage_relays
        )
        DELETE FROM names WHERE identified_object_mrid IN (SELECT mrid FROM mrids);
        """.trimIndent(),
    ),
    targetDatabases = setOf(DatabaseType.CUSTOMER, DatabaseType.DIAGRAM)
)

@Suppress("ObjectPropertyName")
private val `Clean unused name types` = Change(
    listOf(
        "DELETE FROM name_types WHERE name NOT IN (SELECT name_type_name FROM names);"
    ),
    targetDatabases = setOf(DatabaseType.CUSTOMER, DatabaseType.DIAGRAM, DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Drop customer tables` = Change(
    listOf(
        "DROP TABLE customer_agreements;",
        "DROP TABLE customer_agreements_pricing_structures;",
        "DROP TABLE customers;",
        "DROP TABLE pricing_structures;",
        "DROP TABLE pricing_structures_tariffs;",
        "DROP TABLE tariffs;",
    ),
    targetDatabases = setOf(DatabaseType.DIAGRAM, DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Drop diagram tables` = Change(
    listOf(
        "DROP TABLE diagrams;",
        "DROP TABLE diagram_objects;",
        "DROP TABLE diagram_object_points;"
    ),
    targetDatabases = setOf(DatabaseType.CUSTOMER, DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Drop network tables` = Change(
    listOf(
        "DROP TABLE ac_line_segments;",
        "DROP TABLE accumulators;",
        "DROP TABLE analogs;",
        "DROP TABLE asset_organisation_roles_assets;",
        "DROP TABLE asset_owners;",
        "DROP TABLE base_voltages;",
        "DROP TABLE battery_unit;",
        "DROP TABLE breakers;",
        "DROP TABLE busbar_sections;",
        "DROP TABLE cable_info;",
        "DROP TABLE circuits;",
        "DROP TABLE circuits_substations;",
        "DROP TABLE circuits_terminals;",
        "DROP TABLE connectivity_nodes;",
        "DROP TABLE controls;",
        "DROP TABLE current_relays;",
        "DROP TABLE current_transformer_info;",
        "DROP TABLE current_transformers;",
        "DROP TABLE disconnectors;",
        "DROP TABLE discretes;",
        "DROP TABLE distance_relays;",
        "DROP TABLE energy_consumer_phases;",
        "DROP TABLE energy_consumers;",
        "DROP TABLE energy_source_phases;",
        "DROP TABLE energy_sources;",
        "DROP TABLE equipment_equipment_containers;",
        "DROP TABLE equipment_operational_restrictions;",
        "DROP TABLE equipment_usage_points;",
        "DROP TABLE equivalent_branches;",
        "DROP TABLE ev_charging_units;",
        "DROP TABLE fault_indicators;",
        "DROP TABLE feeders;",
        "DROP TABLE fuses;",
        "DROP TABLE geographical_regions;",
        "DROP TABLE grounds;",
        "DROP TABLE ground_disconnectors;",
        "DROP TABLE jumpers;",
        "DROP TABLE junctions;",
        "DROP TABLE linear_shunt_compensators;",
        "DROP TABLE load_break_switches;",
        "DROP TABLE location_street_addresses;",
        "DROP TABLE locations;",
        "DROP TABLE loops;",
        "DROP TABLE loops_substations;",
        "DROP TABLE lv_feeders;",
        "DROP TABLE meters;",
        "DROP TABLE no_load_tests;",
        "DROP TABLE open_circuit_tests;",
        "DROP TABLE operational_restrictions;",
        "DROP TABLE overhead_wire_info;",
        "DROP TABLE per_length_sequence_impedances;",
        "DROP TABLE photo_voltaic_unit;",
        "DROP TABLE poles;",
        "DROP TABLE position_points;",
        "DROP TABLE potential_transformer_info;",
        "DROP TABLE potential_transformers;",
        "DROP TABLE power_electronics_connection;",
        "DROP TABLE power_electronics_connection_phase;",
        "DROP TABLE power_electronics_wind_unit;",
        "DROP TABLE power_transformer_ends;",
        "DROP TABLE power_transformer_end_ratings;",
        "DROP TABLE power_transformer_info;",
        "DROP TABLE power_transformers;",
        "DROP TABLE protection_relay_function_thresholds;",
        "DROP TABLE protection_relay_function_time_limits;",
        "DROP TABLE protection_relay_functions_protected_switches;",
        "DROP TABLE protection_relay_functions_sensors;",
        "DROP TABLE protection_relay_schemes;",
        "DROP TABLE protection_relay_schemes_protection_relay_functions;",
        "DROP TABLE protection_relay_systems;",
        "DROP TABLE ratio_tap_changers;",
        "DROP TABLE reclosers;",
        "DROP TABLE reclose_delays;",
        "DROP TABLE relay_info;",
        "DROP TABLE remote_controls;",
        "DROP TABLE remote_sources;",
        "DROP TABLE series_compensators;",
        "DROP TABLE short_circuit_tests;",
        "DROP TABLE shunt_compensator_info;",
        "DROP TABLE sites;",
        "DROP TABLE streetlights;",
        "DROP TABLE sub_geographical_regions;",
        "DROP TABLE substations;",
        "DROP TABLE switch_info;",
        "DROP TABLE tap_changer_controls;",
        "DROP TABLE terminals;",
        "DROP TABLE transformer_end_info;",
        "DROP TABLE transformer_star_impedance;",
        "DROP TABLE transformer_tank_info;",
        "DROP TABLE usage_points;",
        "DROP TABLE usage_points_end_devices;",
        "DROP TABLE voltage_relays;",
    ),
    targetDatabases = setOf(DatabaseType.CUSTOMER, DatabaseType.DIAGRAM)
)

@Suppress("ObjectPropertyName")
private val `Drop tables shared between customer and network` = Change(
    listOf(
        "DROP TABLE organisations;"
    ),
    targetDatabases = setOf(DatabaseType.DIAGRAM)
)
