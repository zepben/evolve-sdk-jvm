/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets

import com.zepben.evolve.database.paths.DatabaseType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import java.sql.Statement

/**
 * This class has helpers for:
 *
 * 1. Populating the databases for change set 50, which is the first change after the split, so all databases need to be filled with
 * the same data to test the split has correctly cleaned up the data.
 *
 * 2. Validating that tables have been kept or removed.
 *
 * 3. Validating that names have been kept or removed.
 */
object ChangeSet50Helpers {

    //
    // NOTE: We do not need to specify the column names like normal as the schema for this was created by the scripts with a defined order.
    //
    val setUpStatements: List<String> = listOf(
        populateCommonTables(),
        populateCustomerTables(),
        populateDiagramTables(),
        populateNetworkTables(),
        populateSharedTables(),
        useSharedTables(),

        populateCommonNameType(),
        populateCustomerNameType(),
        populateDiagramNameType(),
        populateNetworkNameType(),
        populateSharedNameTypeCustomerDiagram(),
        populateSharedNameTypeCustomerNetwork(),
        populateSharedNameTypeDiagramNetwork(),

        populateCommonNames(),
        populateCustomerNames(),
        populateDiagramNames(),
        populateNetworkNames(),
        populateSharedNamesCustomerDiagram(),
        populateSharedNamesCustomerNetwork(),
        populateSharedNamesDiagramNetwork(),
        createSharedNameObjects()
    ).flatten()

    /**
     * Returns a pair of expected (should have been kept) and unexpected (should have been removed) tables.
     */
    fun tables(type: DatabaseType): Pair<Set<String>, Set<String>> = when (type) {
        // Filter out the organisations table from the network tables as it is also kept by customer databases.
        DatabaseType.CUSTOMER -> listOf(populateCommonTables(), populateCustomerTables(), populateSharedTables()).extractTables() to
            listOf(populateDiagramTables(), populateNetworkTables().filter { !it.contains("INSERT INTO organisations ") }).extractTables()

        DatabaseType.DIAGRAM -> listOf(populateCommonTables(), populateDiagramTables()).extractTables() to
            listOf(populateCustomerTables(), populateNetworkTables(), populateSharedTables()).extractTables()

        // Filter out the organisations table from the customer tables as it is also kept by network databases.
        DatabaseType.NETWORK_MODEL -> listOf(populateCommonTables(), populateNetworkTables(), populateSharedTables()).extractTables() to
            listOf(populateCustomerTables().filter { !it.contains("INSERT INTO organisations ") }, populateDiagramTables()).extractTables()

        else -> throw IllegalStateException("Only accepts the follow database types: CUSTOMERS, DIAGRAMS, NETWORK_MODEL. Received: $type")
    }

    /**
     * Ensure the names and name_types for the database have been kept, and the other database names have been removed.
     */
    fun ensureNames(statement: Statement, type: DatabaseType) {
        val (expectedNameTypes, unexpectedNameTypes) = findExpectedNameTypeValues(type)
        expectedNameTypes.forEach { statement.ensureSelect("name_types", "name = '$it'") }
        unexpectedNameTypes.forEach { statement.ensureSelect("name_types", "name = '$it'", expectedCount = 0) }

        val (expectedNames, unexpectedNames) = findExpectedNameValues(type)
        expectedNames.forEach { statement.ensureSelect("names", "name = '$it'") }
        unexpectedNames.forEach { statement.ensureSelect("names", "name = '$it'", expectedCount = 0) }
    }

    private fun populateCommonTables() = listOf(
        "INSERT INTO metadata_data_sources VALUES ('source', 'version', 'timestamp');",
        "INSERT INTO version VALUES ('49');",
    )

    private fun populateCustomerTables() = listOf(
        "INSERT INTO customer_agreements VALUES ('customer_agreement_mrid', 'name', 'description', 1, 'title', 'created_date_time', 'author_name', 'type', 'status', 'comment', 'customer_mrid');",
        "INSERT INTO customer_agreements_pricing_structures VALUES ('customer_agreement_mrid', 'pricing_structure_mrid');",
        "INSERT INTO customers VALUES ('customer_mrid', 'name', 'description', 1, 'customer_organisation_mrid', 'kind', 2);",
        "INSERT INTO organisations VALUES ('customer_organisation_mrid', 'name', 'description', 1);",
        "INSERT INTO pricing_structures VALUES ('pricing_structure_mrid', 'name', 'description', 1, 'title', 'created_date_time', 'author_name', 'type', 'status', 'comment');",
        "INSERT INTO pricing_structures_tariffs VALUES ('pricing_structure_mrid', 'tariff_mrid');",
        "INSERT INTO tariffs VALUES ('tariff_mrid', 'name', 'description', 1, 'title', 'created_date_time', 'author_name', 'type', 'status', 'comment');",
    )

    private fun populateDiagramTables() = listOf(
        "INSERT INTO diagram_object_points VALUES ('diagram_object_mrid', 'sequence_number', 'x_position', 'y_position');",
        "INSERT INTO diagram_objects VALUES ('diagram_object_mrid', 'name', 'description', 1, 'identified_object_mrid', 'diagram_mrid', 'style', 1.1);",
        "INSERT INTO diagrams VALUES ('diagram_mrid', 'name', 'description', 1, 'diagram_style', 'orientation_kind');",
    )

    private fun populateNetworkTables() = listOf(
        "INSERT INTO ac_line_segments VALUES ('ac_line_segment_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1.1, 'wire_info_mrid', 'per_length_sequence_impedance_mrid');",
        "INSERT INTO accumulators VALUES ('accumulator_mrid', 'name', 'description', 1, 'power_system_resource_mrid', 'remote_source_mrid', 'terminal_mrid', 'phases', 'unit_symbol');",
        "INSERT INTO analogs VALUES ('analog_mrid', 'name', 'description', 1, 'power_system_resource_mrid', 'remote_source_mrid', 'terminal_mrid', 'phases', 'unit_symbol', true);",
        "INSERT INTO asset_organisation_roles_assets VALUES ('asset_organisation_role_mrid', 'asset_mrid');",
        "INSERT INTO asset_owners VALUES ('asset_owner_mrid', 'name', 'description', 1, 'network_organisation_mrid');",
        "INSERT INTO base_voltages VALUES ('base_voltage_mrid', 'name', 'description', 1, 1);",
        "INSERT INTO battery_unit VALUES ('battery_unit_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1.1, 1.1, 'battery_state', 1, 1);",
        "INSERT INTO breakers VALUES ('breaker_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1, 1, 1, 'switch_info_mrid', 1, 1.1);",
        "INSERT INTO busbar_sections VALUES ('busbar_section_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid');",
        "INSERT INTO cable_info VALUES ('cable_info_mrid', 'name', 'description', 1, 1.1, 'material');",
        "INSERT INTO circuits VALUES ('circuit_mrid', 'name', 'description', 1, 'location_mrid', 1, 'loop_mrid');",
        "INSERT INTO circuits_substations VALUES ('circuit_mrid', 'substation_mrid');",
        "INSERT INTO circuits_terminals VALUES ('circuit_mrid', 'terminal_mrid');",
        "INSERT INTO connectivity_nodes VALUES ('connectivity_node_mrid', 'name', 'description', 1);",
        "INSERT INTO controls VALUES ('control_mrid', 'name', 'description', 1, 'power_system_resource_mrid');",
        "INSERT INTO current_relays VALUES ('current_relay_mrid', 'name', 'description', 1, 'location_mrid', 1, 'model', true, 1.1, 'protection_kind', true, 'power_direction', 'relay_info_mrid', 1.1, true, 1.1);",
        "INSERT INTO current_transformer_info VALUES ('current_transformer_info_mrid', 'name', 'description', 1, 'accuracy_class', 1.1, 1, 'ct_class', 1, 1.1, 1.1, 1.1, 1.1, 1.1, 1, 1, 1.1, 'usage');",
        "INSERT INTO current_transformers VALUES ('current_transformer_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'terminal_mrid', 'current_transformer_info_mrid', 1);",
        "INSERT INTO disconnectors VALUES ('disconnector_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1, 1, 1, 'switch_info_mrid');",
        "INSERT INTO discretes VALUES ('discrete_mrid', 'name', 'description', 1, 'power_system_resource_mrid', 'remote_source_mrid', 'terminal_mrid', 'phases', 'unit_symbol');",
        "INSERT INTO distance_relays VALUES ('distance_relay_mrid', 'name', 'description', 1, 'location_mrid', 1, 'model', true, 1.1, 'protection_kind', true, 'power_direction', 'relay_info_mrid', 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1);",
        "INSERT INTO energy_consumer_phases VALUES ('energy_consumer_phase_mrid', 'name', 'description', 1, 'location_mrid', 1, 'energy_consumer_mrid', 'phase', 1.1, 1.1, 1.1, 1.1);",
        "INSERT INTO energy_consumers VALUES ('energy_consumer_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1, true, 1.1, 1.1, 1.1, 1.1, 'phase_connection');",
        "INSERT INTO energy_source_phases VALUES ('energy_source_phase_mrid', 'name', 'description', 1, 'location_mrid', 1, 'energy_source_mrid', 'phase');",
        "INSERT INTO energy_sources VALUES ('energy_source_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, true, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1);",
        "INSERT INTO equipment_equipment_containers VALUES ('equipment_mrid', 'equipment_container_mrid');",
        "INSERT INTO equipment_operational_restrictions VALUES ('equipment_mrid', 'operational_restriction_mrid');",
        "INSERT INTO equipment_usage_points VALUES ('equipment_mrid', 'usage_point_mrid');",
        "INSERT INTO equivalent_branches VALUES ('equivalent_branches_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1);",
        "INSERT INTO ev_charging_units VALUES ('ev_charging_unit_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1.1, 1.1);",
        "INSERT INTO fault_indicators VALUES ('fault_indicator_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'terminal_mrid');",
        "INSERT INTO feeders VALUES ('feeder_mrid', 'name', 'description', 1, 'location_mrid', 1, 'normal_head_terminal_mrid', 'normal_energizing_substation_mrid');",
        "INSERT INTO fuses VALUES ('fuse_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1, 1, 1, 'switch_info_mrid', 'function_mrid');",
        "INSERT INTO geographical_regions VALUES ('geographical_region_mrid', 'name', 'description', 1);",
        "INSERT INTO grounds VALUES ('ground_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid');",
        "INSERT INTO ground_disconnectors VALUES ('ground_disconnector_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1, 1, 1, 'switch_info_mrid');",
        "INSERT INTO jumpers VALUES ('jumper_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1, 1, 1, 'switch_info_mrid');",
        "INSERT INTO junctions VALUES ('junction_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid');",
        "INSERT INTO linear_shunt_compensators VALUES ('linear_shunt_compensator_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', true, 'regulating_control_mrid', 'shunt_compensator_info_mrid', true, 1, 'phase_connection', 1.1, 1.1, 1.1, 1.1, 1.1);",
        "INSERT INTO load_break_switches VALUES ('load_break_switches_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1, 1, 1, 'switch_info_mrid', 1);",
        "INSERT INTO location_street_addresses VALUES ('town_name', 'state_or_province', 'postal_code', 'po_box', 'building_name', 'floor_identification', 'name', 'number', 'suite_number', 'type', 'display_address', 'location_mrid', 'address_field');",
        "INSERT INTO locations VALUES ('location_mrid', 'name', 'description', 1);",
        "INSERT INTO loops VALUES ('loop_mrid', 'name', 'description', 1);",
        "INSERT INTO loops_substations VALUES ('loop_mrid', 'substation_mrid', 'relationship');",
        "INSERT INTO lv_feeders VALUES ('lv_feeder_mrid', 'name', 'description', 1, 'location_mrid', 1, 'normal_head_terminal_mrid');",
        "INSERT INTO meters VALUES ('meter_mrid', 'name', 'description', 1, 'location_mrid', 'customer_mrid', 'service_location_mrid');",
        "INSERT INTO no_load_tests VALUES ('no_load_test_mrid', 'name', 'description', 1, 1, 1.1, 1, 1.1, 1.1, 1, 1);",
        "INSERT INTO open_circuit_tests VALUES ('open_circuit_test_mrid', 'name', 'description', 1, 1, 1.1, 1, 1, 1, 1, 1.1);",
        "INSERT INTO operational_restrictions VALUES ('operational_restriction_mrid', 'name', 'description', 1, 'title', 'created_date_time', 'author_name', 'type', 'status', 'comment');",
        "INSERT INTO organisations VALUES ('network_organisation_mrid', 'name', 'description', 1);",
        "INSERT INTO overhead_wire_info VALUES ('overhead_wire_info_mrid', 'name', 'description', 1, 1.1, 'material');",
        "INSERT INTO per_length_sequence_impedances VALUES ('per_length_sequence_impedance_mrid', 'name', 'description', 1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1);",
        "INSERT INTO photo_voltaic_unit VALUES ('photo_voltaic_unit_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1.1, 1.1);",
        "INSERT INTO poles VALUES ('pole_mrid', 'name', 'description', 1, 'location_mrid', 'classification');",
        "INSERT INTO position_points VALUES ('location_mrid', 1, 1.1, 1.1);",
        "INSERT INTO potential_transformer_info VALUES ('potential_transformer_info_mrid', 'name', 'description', 1, 'accuracy_class', 1.1, 1.1, 1.1, 'pt_class', 1, 1.1);",
        "INSERT INTO potential_transformers VALUES ('potential_transformer_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'terminal_mrid', 'potential_transformer_info_mrid', 'type');",
        "INSERT INTO power_electronics_connection VALUES ('power_electronics_connection_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', true, 'regulating_control_mrid', 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 'inverter_standard', 1, 1.1, 1.1, true, 1, 1, 1, 1, 1.1, 1.1, 1.1, 1.1, true, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, true, 1.1);",
        "INSERT INTO power_electronics_connection_phase VALUES ('power_electronics_connection_phase_mrid', 'name', 'description', 1, 'location_mrid', 1, 'power_electronics_connection_mrid', 1.1, 'phase', 1.1);",
        "INSERT INTO power_electronics_wind_unit VALUES ('power_electronics_wind_unit_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'power_electronics_connection_mrid', 1.1, 1.1);",
        "INSERT INTO power_transformer_ends VALUES ('power_transformer_end_mrid', 'name', 'description', 1, 1, 'terminal_mrid', 'base_voltage_mrid', true, 1.1, 1.1, 'star_impedance_mrid', 'power_transformer_mrid', 'connection_kind', 1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1, 1.1, 1.1);",
        "INSERT INTO power_transformer_end_ratings VALUES ('power_transformer_end_mrid', 'cooling_type', 1);",
        "INSERT INTO power_transformer_info VALUES ('power_transformer_info_mrid', 'name', 'description', 1);",
        "INSERT INTO power_transformers VALUES ('power_transformer_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 'vector_group', 1.1, 'construction_kind', 'function', 'power_transformer_info_mrid');",
        "INSERT INTO protection_relay_function_thresholds VALUES ('protection_relay_function_mrid', 1, 'unit_symbol', 1.1, 'name');",
        "INSERT INTO protection_relay_function_time_limits VALUES ('protection_relay_function_mrid', 1, 1.1);",
        "INSERT INTO protection_relay_functions_protected_switches VALUES ('protection_relay_function_mrid', 'protected_switch_mrid');",
        "INSERT INTO protection_relay_functions_sensors VALUES ('protection_relay_function_mrid', 'sensor_mrid');",
        "INSERT INTO protection_relay_schemes VALUES ('protection_relay_scheme_mrid', 'name', 'description', 1, 'system_mrid');",
        "INSERT INTO protection_relay_schemes_protection_relay_functions VALUES ('protection_relay_scheme_mrid', 'protection_relay_function_mrid');",
        "INSERT INTO protection_relay_systems VALUES ('protection_relay_system_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'protection_kind');",
        "INSERT INTO ratio_tap_changers VALUES ('ratio_tap_changer_mrid', 'name', 'description', 1, 'location_mrid', 1, true, 1, 1, 1, 1, 1, 1.1, 'tap_changer_control_mrid', 'transformer_end_mrid', 1.1);",
        "INSERT INTO reclosers VALUES ('recloser_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1, 1, 1, 'switch_info_mrid', 1);",
        "INSERT INTO reclose_delays VALUES ('relay_info_mrid', 1.1, 1);",
        "INSERT INTO relay_info VALUES ('relay_info_mrid', 'name', 'description', 1, 'curve_setting', true);",
        "INSERT INTO remote_controls VALUES ('remote_control_mrid', 'name', 'description', 1, 'control_mrid');",
        "INSERT INTO remote_sources VALUES ('remote_source_mrid', 'name', 'description', 1, 'measurement_mrid');",
        "INSERT INTO series_compensators VALUES ('series_compensator_mrid', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid', 1.1, 1.1, 1.1, 1.1, 1, 1);",
        "INSERT INTO short_circuit_tests VALUES ('short_circuit_test_mrid', 'name', 'description', 1, 1, 1.1, 1.1, 1, 1, 1.1, 1.1, 1, 1, 1, 1.1, 1.1);",
        "INSERT INTO shunt_compensator_info VALUES ('shunt_compensator_info_mrid', 'name', 'description', 1, 1, 1, 1, 1);",
        "INSERT INTO sites VALUES ('site_mrid', 'name', 'description', 1, 'location_mrid', 1);",
        "INSERT INTO streetlights VALUES ('streetlight_mrid', 'name', 'description', 1, 'location_mrid', 'pole_mrid', 'lamp_kind', 1.1);",
        "INSERT INTO sub_geographical_regions VALUES ('sub_geographical_region_mrid', 'name', 'description', 1, 'geographical_region_mrid');",
        "INSERT INTO substations VALUES ('substation_mrid', 'name', 'description', 1, 'location_mrid', 1, 'sub_geographical_region_mrid');",
        "INSERT INTO switch_info VALUES ('switch_info_mrid', 'name', 'description', 1, 1.1);",
        "INSERT INTO tap_changer_controls VALUES ('tap_changer_control_mrid', 'name', 'description', 1, 'location_mrid', 1, true, 'mode', 'monitored_phase', 1.1, 1.1, true, 1.1, 1.1, 1.1, 'terminal_mrid', 1.1, true, 1.1, 1.1, 1.1, 1.1, true, 1.1, true);",
        "INSERT INTO terminals VALUES ('terminal_mrid', 'name', 'description', 1, 'conducting_equipment_mrid', 1, 'connectivity_node_mrid', 'phases');",
        "INSERT INTO transformer_end_info VALUES ('transformer_end_info_mrid', 'name', 'description', 1, 'connection_kind', 1, 1, 1, 1, 1.1, 1, 1, 1, 'transformer_tank_info_mrid', 'energised_end_no_load_tests', 'energised_end_short_circuit_tests', 'grounded_end_short_circuit_tests', 'open_end_open_circuit_tests', 'energised_end_open_circuit_tests');",
        "INSERT INTO transformer_star_impedance VALUES ('transformer_star_impedance_mrid', 'name', 'description', 1, 1.1, 1.1, 1.1, 1.1, 'transformer_end_info_mrid');",
        "INSERT INTO transformer_tank_info VALUES ('transformer_tank_info_mrid', 'name', 'description', 1, 'power_transformer_info_mrid');",
        "INSERT INTO usage_points VALUES ('usage_point_mrid', 'name', 'description', 1, 'location_mrid', true, 'connection_category', 1, 1);",
        "INSERT INTO usage_points_end_devices VALUES ('usage_point_mrid', 'end_device_mrid');",
        "INSERT INTO voltage_relays VALUES ('voltage_relay_mrid', 'name', 'description', 1, 'location_mrid', 1, 'model', true, 1.1, 'protection_kind', true, 'power_direction', 'relay_info_mrid');",
    )

    private fun populateSharedTables() = listOf(
        "INSERT INTO organisations VALUES ('shared_organisation_mrid', 'name', 'description', 1);",
    )

    private fun useSharedTables() = listOf(
        "INSERT INTO customers VALUES ('customer_mrid_2', 'name', 'description', 1, 'shared_organisation_mrid', 'kind', 2);",
        "INSERT INTO asset_owners VALUES ('asset_owner_mrid_2', 'name', 'description', 1, 'shared_organisation_mrid');",
    )

    private fun populateCommonNameType() = listOf(
        "INSERT INTO name_types VALUES ('common_name_type', 'description');",
    )

    private fun populateCustomerNameType() = listOf(
        "INSERT INTO name_types VALUES ('customer_name_type', 'description');",
    )

    private fun populateDiagramNameType() = listOf(
        "INSERT INTO name_types VALUES ('diagram_name_type', 'description');",
    )

    private fun populateNetworkNameType() = listOf(
        "INSERT INTO name_types VALUES ('network_name_type', 'description');",
    )

    private fun populateSharedNameTypeCustomerDiagram() = listOf(
        "INSERT INTO name_types VALUES ('customer_diagram_name_type', 'description');",
    )

    private fun populateSharedNameTypeCustomerNetwork() = listOf(
        "INSERT INTO name_types VALUES ('customer_network_name_type', 'description');",
    )

    private fun populateSharedNameTypeDiagramNetwork() = listOf(
        "INSERT INTO name_types VALUES ('diagram_network_name_type', 'description');",
    )

    private fun populateCommonNames() = listOf(
        "INSERT INTO names VALUES ('customer_mrid_3_name', 'customer_mrid_3', 'common_name_type');",
        "INSERT INTO names VALUES ('diagram_mrid_3_name', 'diagram_mrid_3', 'common_name_type');",
        "INSERT INTO names VALUES ('junction_mrid_3_name', 'junction_mrid_3', 'common_name_type');",
    )

    private fun populateCustomerNames() = listOf(
        "INSERT INTO names VALUES ('customer_agreement_mrid_name', 'customer_agreement_mrid', 'customer_name_type');",
        "INSERT INTO names VALUES ('customer_mrid_name', 'customer_mrid', 'customer_name_type');",
        "INSERT INTO names VALUES ('customer_organisation_mrid_name', 'customer_organisation_mrid', 'customer_name_type');",
        "INSERT INTO names VALUES ('pricing_structure_mrid_name', 'pricing_structure_mrid', 'customer_name_type');",
        "INSERT INTO names VALUES ('tariff_mrid_name', 'tariff_mrid', 'customer_name_type');",
    )

    private fun populateDiagramNames() = listOf(
        "INSERT INTO names VALUES ('diagram_object_mrid_name', 'diagram_object_mrid', 'diagram_name_type');",
        "INSERT INTO names VALUES ('diagram_mrid_name', 'diagram_mrid', 'diagram_name_type');",
    )

    private fun populateNetworkNames() = listOf(
        "INSERT INTO names VALUES ('ac_line_segment_mrid_name', 'ac_line_segment_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('accumulator_mrid_name', 'accumulator_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('analog_mrid_name', 'analog_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('asset_owner_mrid_name', 'asset_owner_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('base_voltage_mrid_name', 'base_voltage_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('battery_unit_mrid_name', 'battery_unit_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('breaker_mrid_name', 'breaker_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('busbar_section_mrid_name', 'busbar_section_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('cable_info_mrid_name', 'cable_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('circuit_mrid_name', 'circuit_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('connectivity_node_mrid_name', 'connectivity_node_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('control_mrid_name', 'control_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('current_relay_mrid_name', 'current_relay_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('current_transformer_info_mrid_name', 'current_transformer_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('current_transformer_mrid_name', 'current_transformer_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('disconnector_mrid_name', 'disconnector_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('discrete_mrid_name', 'discrete_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('distance_relay_mrid_name', 'distance_relay_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('energy_consumer_phase_mrid_name', 'energy_consumer_phase_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('energy_consumer_mrid_name', 'energy_consumer_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('energy_source_phase_mrid_name', 'energy_source_phase_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('energy_source_mrid_name', 'energy_source_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('equivalent_branches_mrid_name', 'equivalent_branches_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('ev_charging_unit_mrid_name', 'ev_charging_unit_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('fault_indicator_mrid_name', 'fault_indicator_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('feeder_mrid_name', 'feeder_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('fuse_mrid_name', 'fuse_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('geographical_region_mrid_name', 'geographical_region_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('ground_mrid_name', 'ground_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('ground_disconnector_mrid_name', 'ground_disconnector_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('jumper_mrid_name', 'jumper_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('junction_mrid_name', 'junction_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('linear_shunt_compensator_mrid_name', 'linear_shunt_compensator_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('load_break_switches_mrid_name', 'load_break_switches_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('location_mrid_name', 'location_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('loop_mrid_name', 'loop_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('lv_feeder_mrid_name', 'lv_feeder_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('meter_mrid_name', 'meter_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('no_load_test_mrid_name', 'no_load_test_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('open_circuit_test_mrid_name', 'open_circuit_test_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('operational_restriction_mrid_name', 'operational_restriction_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('network_organisation_mrid_name', 'network_organisation_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('overhead_wire_info_mrid_name', 'overhead_wire_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('per_length_sequence_impedance_mrid_name', 'per_length_sequence_impedance_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('photo_voltaic_unit_mrid_name', 'photo_voltaic_unit_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('pole_mrid_name', 'pole_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('potential_transformer_info_mrid_name', 'potential_transformer_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('potential_transformer_mrid_name', 'potential_transformer_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('power_electronics_connection_mrid_name', 'power_electronics_connection_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('power_electronics_connection_phase_mrid_name', 'power_electronics_connection_phase_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('power_electronics_wind_unit_mrid_name', 'power_electronics_wind_unit_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('power_transformer_end_mrid_name', 'power_transformer_end_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('power_transformer_info_mrid_name', 'power_transformer_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('power_transformer_mrid_name', 'power_transformer_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('protection_relay_scheme_mrid_name', 'protection_relay_scheme_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('protection_relay_system_mrid_name', 'protection_relay_system_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('ratio_tap_changer_mrid_name', 'ratio_tap_changer_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('recloser_mrid_name', 'recloser_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('relay_info_mrid_name', 'relay_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('remote_control_mrid_name', 'remote_control_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('remote_source_mrid_name', 'remote_source_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('series_compensator_mrid_name', 'series_compensator_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('short_circuit_test_mrid_name', 'short_circuit_test_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('shunt_compensator_info_mrid_name', 'shunt_compensator_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('site_mrid_name', 'site_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('streetlight_mrid_name', 'streetlight_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('sub_geographical_region_mrid_name', 'sub_geographical_region_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('substation_mrid_name', 'substation_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('switch_info_mrid_name', 'switch_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('tap_changer_control_mrid_name', 'tap_changer_control_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('terminal_mrid_name', 'terminal_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('transformer_end_info_mrid_name', 'transformer_end_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('transformer_star_impedance_mrid_name', 'transformer_star_impedance_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('transformer_tank_info_mrid_name', 'transformer_tank_info_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('usage_point_mrid_name', 'usage_point_mrid', 'network_name_type');",
        "INSERT INTO names VALUES ('voltage_relay_mrid_name', 'voltage_relay_mrid', 'network_name_type');",
    )

    private fun populateSharedNamesCustomerDiagram() = listOf(
        "INSERT INTO names VALUES ('customer_mrid_4_name', 'customer_mrid_4', 'customer_diagram_name_type');",
        "INSERT INTO names VALUES ('diagram_mrid_4_name', 'diagram_mrid_4', 'customer_diagram_name_type');",
    )

    private fun populateSharedNamesCustomerNetwork() = listOf(
        "INSERT INTO names VALUES ('customer_mrid_5_name', 'customer_mrid_5', 'customer_network_name_type');",
        "INSERT INTO names VALUES ('junction_mrid_5_name', 'junction_mrid_5', 'customer_network_name_type');",
    )

    private fun populateSharedNamesDiagramNetwork() = listOf(
        "INSERT INTO names VALUES ('diagram_mrid_6_name', 'diagram_mrid_6', 'diagram_network_name_type');",
        "INSERT INTO names VALUES ('junction_mrid_6_name', 'junction_mrid_6', 'diagram_network_name_type');",
    )

    private fun createSharedNameObjects() = listOf(
        "INSERT INTO customers VALUES ('customer_mrid_3', 'name', 'description', 1, 'customer_organisation_mrid', 'kind', 2);",
        "INSERT INTO customers VALUES ('customer_mrid_4', 'name', 'description', 1, 'customer_organisation_mrid', 'kind', 2);",
        "INSERT INTO customers VALUES ('customer_mrid_5', 'name', 'description', 1, 'customer_organisation_mrid', 'kind', 2);",

        "INSERT INTO diagrams VALUES ('diagram_mrid_3', 'name', 'description', 1, 'diagram_style', 'orientation_kind');",
        "INSERT INTO diagrams VALUES ('diagram_mrid_4', 'name', 'description', 1, 'diagram_style', 'orientation_kind');",
        "INSERT INTO diagrams VALUES ('diagram_mrid_6', 'name', 'description', 1, 'diagram_style', 'orientation_kind');",

        "INSERT INTO junctions VALUES ('junction_mrid_3', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid');",
        "INSERT INTO junctions VALUES ('junction_mrid_5', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid');",
        "INSERT INTO junctions VALUES ('junction_mrid_6', 'name', 'description', 1, 'location_mrid', 1, true, true, 'commissioned_date', 'base_voltage_mrid');",
    )

    private fun findExpectedNameTypeValues(type: DatabaseType): Pair<List<String>, List<String>> = when (type) {
        DatabaseType.CUSTOMER ->
            listOf(
                populateCommonNameType(),
                populateCustomerNameType(),
                populateSharedNameTypeCustomerDiagram(),
                populateSharedNameTypeCustomerNetwork(),
            ).extractValues() to
                listOf(
                    populateDiagramNameType(),
                    populateNetworkNameType(),
                    populateSharedNameTypeDiagramNetwork(),
                ).extractValues()

        DatabaseType.DIAGRAM ->
            listOf(
                populateCommonNameType(),
                populateDiagramNameType(),
                populateSharedNameTypeCustomerDiagram(),
                populateSharedNameTypeDiagramNetwork(),
            ).extractValues() to
                listOf(
                    populateCustomerNameType(),
                    populateNetworkNameType(),
                    populateSharedNameTypeCustomerNetwork(),
                ).extractValues()

        DatabaseType.NETWORK_MODEL ->
            listOf(
                populateCommonNameType(),
                populateNetworkNameType(),
                populateSharedNameTypeCustomerNetwork(),
                populateSharedNameTypeDiagramNetwork(),
            ).extractValues() to
                listOf(
                    populateCustomerNameType(),
                    populateDiagramNameType(),
                    populateSharedNameTypeCustomerDiagram(),
                ).extractValues()

        else -> throw IllegalStateException("Only accepts the follow database types: CUSTOMERS, DIAGRAMS, NETWORK_MODEL. Received: $type")
    }

    private fun findExpectedNameValues(type: DatabaseType): Pair<List<String>, List<String>> = when (type) {
        DatabaseType.CUSTOMER ->
            listOf(
                populateCommonNames().filter { it.contains("customer_mrid") },
                populateCustomerNames(),
                populateSharedNamesCustomerDiagram().filter { it.contains("customer_mrid") },
                populateSharedNamesCustomerNetwork().filter { it.contains("customer_mrid") }
            ).extractValues() to
                listOf(
                    populateDiagramNames().filter { !it.contains("customer_mrid") },
                    populateNetworkNames(),
                    populateSharedNamesCustomerDiagram().filter { !it.contains("customer_mrid") },
                    populateSharedNamesCustomerNetwork().filter { !it.contains("customer_mrid") },
                    populateSharedNamesDiagramNetwork()
                ).extractValues()

        DatabaseType.DIAGRAM ->
            listOf(
                populateCommonNames().filter { it.contains("diagram_mrid") },
                populateDiagramNames(),
                populateSharedNamesCustomerDiagram().filter { it.contains("diagram_mrid") },
                populateSharedNamesDiagramNetwork().filter { it.contains("diagram_mrid") }
            ).extractValues() to
                listOf(
                    populateCustomerNames().filter { !it.contains("diagram_mrid") },
                    populateNetworkNames(),
                    populateSharedNamesCustomerDiagram().filter { !it.contains("diagram_mrid") },
                    populateSharedNamesCustomerNetwork(),
                    populateSharedNamesDiagramNetwork().filter { !it.contains("diagram_mrid") }
                ).extractValues()

        DatabaseType.NETWORK_MODEL ->
            listOf(
                populateCommonNames().filter { it.contains("junction_mrid") },
                populateNetworkNames(),
                populateSharedNamesCustomerNetwork().filter { it.contains("junction_mrid") },
                populateSharedNamesDiagramNetwork().filter { it.contains("junction_mrid") }
            ).extractValues() to
                listOf(
                    populateCustomerNames().filter { !it.contains("junction_mrid") },
                    populateDiagramNames(),
                    populateSharedNamesCustomerDiagram(),
                    populateSharedNamesCustomerNetwork().filter { !it.contains("junction_mrid") },
                    populateSharedNamesDiagramNetwork().filter { !it.contains("junction_mrid") }
                ).extractValues()

        else -> throw IllegalStateException("Only accepts the follow database types: CUSTOMERS, DIAGRAMS, NETWORK_MODEL. Received: $type")
    }

    private fun Iterable<List<String>>.extractTables(): Set<String> =
        asSequence()
            .flatten()
            .map {
                val second = it.indexOf(" VALUES") - 1
                it.substring(12..second)
            }
            .toSet()

    private fun List<List<String>>.extractValues(): List<String> =
        asSequence()
            .flatten()
            .map {
                val first = it.indexOf("'") + 1
                val second = it.indexOf("'", first) - 1
                it.substring(first..second)
            }
            .toList()

    private fun Statement.ensureSelect(table: String, condition: String, expectedCount: Int = 1) {
        executeQuery("SELECT count(*) FROM $table WHERE $condition;").use { rs ->
            rs.next()
            assertThat("Table $table entries matching `$condition`", rs.getInt(1), equalTo(expectedCount))
        }
    }

}
