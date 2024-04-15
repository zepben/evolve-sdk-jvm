/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets.combined

import com.zepben.evolve.database.getInstant
import com.zepben.evolve.database.getNullableBoolean
import com.zepben.evolve.database.getNullableDouble
import com.zepben.evolve.database.getNullableString
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.ChangeSetValidator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import java.sql.Statement
import java.time.Instant

object ChangeSet48Validator : ChangeSetValidator {
    override fun setUpStatements(): List<String> = listOf(
        """
           INSERT INTO power_transformer_ends (mrid, name, description, num_diagram_objects, end_number, terminal_mrid, base_voltage_mrid, grounded, 
           r_ground, x_ground, star_impedance_mrid, power_transformer_mrid, connection_kind, phase_angle_clock, b, b0, g, g0, R, R0, rated_s, rated_u, X, X0) 
           VALUES ('pt1', 'name', 'desc', 0, 0, 't1', 'bv1', true, 0.1, 0.2, 'si', 'pt1', 'D', 3, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10, 11, 12.0, 13.0)
        """.trimIndent(),
        """
           INSERT INTO power_transformer_ends (mrid, name, description, num_diagram_objects, end_number, terminal_mrid, base_voltage_mrid, grounded, 
           r_ground, x_ground, star_impedance_mrid, power_transformer_mrid, connection_kind, phase_angle_clock, b, b0, g, g0, R, R0, rated_s, rated_u, X, X0) 
           VALUES ('pt2', 'name', 'desc', 0, 0, 't1', 'bv1', true, 0.1, 0.2, 'si', 'pt2', 'D', 3, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, null, 11, 12.0, 13.0)
        """.trimIndent(),

        """
           INSERT INTO ac_line_segments (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, length, wire_info_mrid, per_length_sequence_impedance_mrid) 
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 10.0, 'wi1', 'plsi1')
        """.trimIndent(),
        """
           INSERT INTO power_transformers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, vector_group, transformer_utilisation, construction_kind, function, power_transformer_info_mrid)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 'DYN1', 0.0, 'unknown', 'other', 'pti1')
        """.trimIndent(),
        """
           INSERT INTO linear_shunt_compensators (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, control_enabled, shunt_compensator_info_mrid, grounded, nom_u, phase_connection, sections, b0_per_section, b_per_section, 
           g0_per_section, g_per_section)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', true, 's1', true, 30, 'D', 1.1, 1.2, 1.3, 1.4, 1.5)
        """.trimIndent(),
        """
           INSERT INTO energy_consumers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, customer_count, grounded, p, q, p_fixed, q_fixed, phase_connection)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 0, true, 0.1, 0.2, 0.3, 0.4, 'D')
        """.trimIndent(),
        """
           INSERT INTO energy_sources (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, active_power, reactive_power, voltage_angle, voltage_magnitude, p_max, p_min, r, r0, rn, x, x0, xn, is_external_grid, r_min,
           rn_min, r0_min, x_min, xn_min, x0_min, r_max, rn_max, r0_max, x_max, xn_max, x0_max)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, true, 13.0, 14.0, 
           15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0)
        """.trimIndent(),
        """
           INSERT INTO junctions (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1')
        """.trimIndent(),
        """
           INSERT INTO busbar_sections (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1')
        """.trimIndent(),
        """
           INSERT INTO disconnectors (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3)
        """.trimIndent(),
        """
           INSERT INTO jumpers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3)
        """.trimIndent(),
        """
           INSERT INTO fuses (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3)
        """.trimIndent(),
        """
           INSERT INTO load_break_switches (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, breaking_capacity)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, 4)
        """.trimIndent(),
        """
           INSERT INTO breakers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, breaking_capacity)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, 4)
        """.trimIndent(),
        """
           INSERT INTO reclosers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, breaking_capacity)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, 4)
        """.trimIndent(),
        """
           INSERT INTO equivalent_branches (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, negative_r12, negative_r21, negative_x12, negative_x21, positive_r12, positive_r21, positive_x12, positive_x21, r, r21, x, x21,
           zero_r12, zero_r21, zero_x12, zero_x21)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 
           16.0)
        """.trimIndent(),
        """
           INSERT INTO photo_voltaic_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           power_electronics_connection_mrid, max_p, min_p)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'pec1', 1.0, 2.0)
        """.trimIndent(),
        """
           INSERT INTO power_electronics_wind_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           power_electronics_connection_mrid, max_p, min_p)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'pec1', 1.0, 2.0)
        """.trimIndent(),
        """
           INSERT INTO battery_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           power_electronics_connection_mrid, max_p, min_p, battery_state, rated_e, stored_e)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'pec1', 1.0, 2.0, 'UNKNOWN', 3, 4)
        """.trimIndent(),
        """
           INSERT INTO usage_points (mrid, name, description, num_diagram_objects, location_mrid, is_virtual, connection_category) 
           VALUES ('id2', 'name', 'desc', 0, 'l_id', true, 'test')
        """.trimIndent(),
        """
           INSERT INTO current_relays (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           relay_delay_time, protection_kind, current_limit_1, inverse_time_flag, time_delay_1, current_relay_info_mrid) 
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 2.3, 'EF', 50.0, null, 1.3, 'cri1')
        """.trimIndent(),
        """
           INSERT INTO fault_indicators (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           terminal_mrid)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'terminal1')
        """.trimIndent(),
        """
           INSERT INTO current_transformers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           terminal_mrid, current_transformer_info_mrid, core_burden)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'terminal1', 'cti', 1)
        """.trimIndent(),
        """
           INSERT INTO potential_transformers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           terminal_mrid, potential_transformer_info_mrid, type)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'terminal1', 'pti', 'UNKNOWN')
        """.trimIndent(),
        """
           INSERT INTO power_electronics_connection(mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, control_enabled, max_i_fault, max_q, min_q, p, q, rated_s, rated_u)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', true, 1, 2.0, 3.0, 4.0, 5.0, 6, 7)
        """.trimIndent(),
        """
           INSERT INTO ratio_tap_changers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, control_enabled, high_step, low_step, 
           neutral_step, neutral_u, normal_step, step, transformer_end_mrid, step_voltage_increment)
           VALUES ('id2', 'name', 'desc', 0, 'l_id',  0, true, 1, 2, 3, 4, 5, 6.0, 'te2', 7.0)
        """.trimIndent(),
    )

    override fun populateStatements(): List<String> = listOf(
        """
            INSERT INTO tap_changer_controls (mrid, name, description, num_diagram_objects, location_mrid, num_controls, discrete, mode, monitored_phase,
            target_deadband, target_value, enabled, max_allowed_target_value, min_allowed_target_value, terminal_mrid, limit_voltage, line_drop_compensation, 
            line_drop_r, line_drop_x, reverse_line_drop_r, reverse_line_drop_x, forward_ldc_blocking, time_delay, co_generation_enabled) 
            VALUES ('id1', 'name', 'desc', 1, 'l_id', 2, null, 'UNKNOWN_CONTROL_MODE', 'ABC', 1.0, 2.0, true, 3.0, 4.0, 'term1', 5.0, null, 6.0, 7.0, 8.0, 9.0, 
            null, 10.0, null)
        """.trimIndent(),
        """
            INSERT INTO ev_charging_units (mrid, name, description, num_diagram_objects, location_mrid, num_controls,  normally_in_service, in_service,
            commissioned_date, power_electronics_connection_mrid, max_p, min_p, commissioned_date) VALUES
            ('id1', 'name', 'desc', 1, 'l_id', 2, true, false, '2020-01-01T00:00:00.00Z', 'pec1', 1.0, 2.0, '2020-01-01')
        """.trimIndent(),

        "INSERT INTO reclose_delays (current_relay_info_mrid, reclose_delay, sequence_number) VALUES ('id1', 1.3, 0)",
        "INSERT INTO power_transformer_end_ratings(power_transformer_end_mrid, cooling_type, rated_s) VALUES ('id1', 'ONAF', 10)",

        """
           INSERT INTO ac_line_segments (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, length, wire_info_mrid, per_length_sequence_impedance_mrid, commissioned_date) 
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 10.0, 'wi1', 'plsi1', '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO power_transformers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, vector_group, transformer_utilisation, construction_kind, function, power_transformer_info_mrid, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 'DYN1', 0.0, 'unknown', 'other', 'pti1', '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO linear_shunt_compensators (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, control_enabled, shunt_compensator_info_mrid, grounded, nom_u, phase_connection, sections, b0_per_section, b_per_section, 
           g0_per_section, g_per_section, commissioned_date, regulating_control_mrid)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', true, 's1', true, 30, 'D', 1.1, 1.2, 1.3, 1.4, 1.5, '2020-01-01', 'rc1')
        """.trimIndent(),
        """
           INSERT INTO energy_consumers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, customer_count, grounded, p, q, p_fixed, q_fixed, phase_connection, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 0, true, 0.1, 0.2, 0.3, 0.4, 'D', '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO energy_sources (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, active_power, reactive_power, voltage_angle, voltage_magnitude, p_max, p_min, r, r0, rn, x, x0, xn, is_external_grid, r_min,
           rn_min, r0_min, x_min, xn_min, x0_min, r_max, rn_max, r0_max, x_max, xn_max, x0_max, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, true, 13.0, 14.0, 
           15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO junctions (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO busbar_sections (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO disconnectors (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO jumpers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO fuses (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO load_break_switches (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, breaking_capacity, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, 4, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO breakers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, breaking_capacity, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, 4, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO reclosers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, normal_open, open, switch_info_mrid, rated_current, breaking_capacity, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1, 2, 'swi', 3, 4, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO equivalent_branches (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, negative_r12, negative_r21, negative_x12, negative_x21, positive_r12, positive_r21, positive_x12, positive_x21, r, r21, x, x21,
           zero_r12, zero_r21, zero_x12, zero_x21, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 
           16.0, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO photo_voltaic_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           power_electronics_connection_mrid, max_p, min_p, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'pec1', 1.0, 2.0, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO power_electronics_wind_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           power_electronics_connection_mrid, max_p, min_p, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'pec1', 1.0, 2.0, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO battery_unit (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           power_electronics_connection_mrid, max_p, min_p, battery_state, rated_e, stored_e, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'pec1', 1.0, 2.0, 'UNKNOWN', 3, 4, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO usage_points (mrid, name, description, num_diagram_objects, location_mrid, is_virtual, connection_category, rated_power, approved_inverter_capacity) 
           VALUES ('id1', 'name', 'desc', 0, 'l_id', true, 'test', 50, 500)
        """.trimIndent(),
        """
           INSERT INTO current_relays (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           relay_delay_time, protection_kind, current_limit_1, inverse_time_flag, time_delay_1, current_relay_info_mrid, directable, power_direction, 
           commissioned_date) 
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 2.3, 'EF', 50.0, null, 1.3, 'cri1', true, 'FORWARD', '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO fault_indicators (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           terminal_mrid, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'terminal1', '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO current_transformers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           terminal_mrid, current_transformer_info_mrid, core_burden, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'terminal1', 'cti', 1, '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO potential_transformers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           terminal_mrid, potential_transformer_info_mrid, type, commissioned_date)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'terminal1', 'pti', 'UNKNOWN', '2020-01-01')
        """.trimIndent(),
        """
           INSERT INTO power_electronics_connection(mrid, name, description, num_diagram_objects, location_mrid, num_controls, normally_in_service, in_service, 
           base_voltage_mrid, control_enabled, max_i_fault, max_q, min_q, p, q, rated_s, rated_u, inverter_standard, sustain_op_overvolt_limit, 
           stop_at_over_freq, stop_at_under_freq, inv_volt_watt_resp_mode, inv_watt_resp_v1, inv_watt_resp_v2, inv_watt_resp_v3, inv_watt_resp_v4,
           inv_watt_resp_p_at_v1, inv_watt_resp_p_at_v2, inv_watt_resp_p_at_v3, inv_watt_resp_p_at_v4, inv_volt_var_resp_mode, inv_var_resp_v1, inv_var_resp_v2,
           inv_var_resp_v3, inv_var_resp_v4, inv_var_resp_q_at_v1, inv_var_resp_q_at_v2, inv_var_resp_q_at_v3, inv_var_resp_q_at_v4, inv_reactive_power_mode,
           inv_fix_reactive_power, commissioned_date, regulating_control_mrid)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, true, 'bv1', true, 1, 2.0, 3.0, 4.0, 5.0, 6, 7, "standard", 8, 9.0, 10.0, true, 11, 12, 13, 14, 
           15.0, 16.0, 17.0, 18.0, true, 19, 20, 21, 22, 23.0, 24.0, 25.0, 26.0, true, 27.0, '2020-01-01', 'rc1')
        """.trimIndent(),
        """
           INSERT INTO ratio_tap_changers (mrid, name, description, num_diagram_objects, location_mrid, num_controls, control_enabled, high_step, low_step, 
           neutral_step, neutral_u, normal_step, step, transformer_end_mrid, step_voltage_increment, tap_changer_control_mrid)
           VALUES ('id1', 'name', 'desc', 0, 'l_id',  0, true, 1, 2, 3, 4, 5, 6.0, 'te1', 7.0, 'tc1')
        """.trimIndent(),

        )

    override fun validate(statement: Statement) {
        ensureTables(statement, "tap_changer_controls", "ev_charging_units", "reclose_delays", "power_transformer_end_ratings")

        ensureIndexes(
            statement, "tap_changer_controls_mrid", "tap_changer_controls_name", "ev_charging_units_mrid", "ev_charging_units_name",
            "ev_charging_units_power_electronics_connection_mrid", "reclose_delays_current_relay_info_mrid_sequence_number",
            "reclose_delays_current_relay_info_mrid", "power_transformer_end_ratings_power_transformer_end_mrid_cooling_type",
            "power_transformer_end_ratings_power_transformer_end_mrid"
        )
        ensureColumns(statement, "power_transformer_ends", "rated_s", present = false)

        validateRows(statement, "SELECT * FROM reclose_delays",
            { rs ->
                assertThat(rs.getString("current_relay_info_mrid"), equalTo("id1"))
                assertThat(rs.getDouble("reclose_delay"), equalTo(1.3))
                assertThat(rs.getInt("sequence_number"), equalTo(0))
            }
        )

        validateRows(statement, "SELECT * FROM power_transformer_end_ratings",
            { rs ->
                assertThat(rs.getString("power_transformer_end_mrid"), equalTo("pt1"))
                assertThat(rs.getString("cooling_type"), equalTo("UNKNOWN_COOLING_TYPE"))
                assertThat(rs.getInt("rated_s"), equalTo(10))
            },
            // pt2 had null rated_s, so should be no result for it.
            { rs ->
                assertThat(rs.getString("power_transformer_end_mrid"), equalTo("id1"))
                assertThat(rs.getString("cooling_type"), equalTo("ONAF"))
                assertThat(rs.getInt("rated_s"), equalTo(10))
            }
        )

        validateRows(statement, "SELECT * FROM tap_changer_controls",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getNullableBoolean("discrete"), equalTo(null))
                assertThat(rs.getString("mode"), equalTo("UNKNOWN_CONTROL_MODE"))
                assertThat(rs.getString("monitored_phase"), equalTo("ABC"))
                assertThat(rs.getNullableDouble("target_deadband"), equalTo(1.0))
                assertThat(rs.getNullableDouble("target_value"), equalTo(2.0))
                assertThat(rs.getNullableBoolean("enabled"), equalTo(true))
                assertThat(rs.getNullableDouble("max_allowed_target_value"), equalTo(3.0))
                assertThat(rs.getNullableDouble("min_allowed_target_value"), equalTo(4.0))
                assertThat(rs.getNullableString("terminal_mrid"), equalTo("term1"))
                assertThat(rs.getNullableDouble("limit_voltage"), equalTo(5.0))
                assertThat(rs.getNullableBoolean("line_drop_compensation"), equalTo(null))
                assertThat(rs.getNullableDouble("line_drop_r"), equalTo(6.0))
                assertThat(rs.getNullableDouble("line_drop_x"), equalTo(7.0))
                assertThat(rs.getNullableDouble("reverse_line_drop_r"), equalTo(8.0))
                assertThat(rs.getNullableDouble("reverse_line_drop_x"), equalTo(9.0))
                assertThat(rs.getNullableBoolean("forward_ldc_blocking"), equalTo(null))
                assertThat(rs.getNullableDouble("time_delay"), equalTo(10.0))
                assertThat(rs.getNullableBoolean("co_generation_enabled"), equalTo(null))
            }
        )

        validateRows(statement, "SELECT * FROM ev_charging_units",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(1))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(2))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(false))
                assertThat(rs.getInstant("commissioned_date"), equalTo(Instant.parse("2020-01-01T00:00:00.00Z")))
                assertThat(rs.getString("power_electronics_connection_mrid"), equalTo("pec1"))
                assertThat(rs.getNullableDouble("max_p"), equalTo(1.0))
                assertThat(rs.getNullableDouble("min_p"), equalTo(2.0))
            }
        )


        validateRows(statement, "SELECT * FROM ac_line_segments",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getDouble("length"), equalTo(10.0))
                assertThat(rs.getString("wire_info_mrid"), equalTo("wi1"))
                assertThat(rs.getString("per_length_sequence_impedance_mrid"), equalTo("plsi1"))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getDouble("length"), equalTo(10.0))
                assertThat(rs.getString("wire_info_mrid"), equalTo("wi1"))
                assertThat(rs.getString("per_length_sequence_impedance_mrid"), equalTo("plsi1"))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM power_transformers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getString("vector_group"), equalTo("DYN1"))
                assertThat(rs.getDouble("transformer_utilisation"), equalTo(0.0))
                assertThat(rs.getString("construction_kind"), equalTo("unknown"))
                assertThat(rs.getString("function"), equalTo("other"))
                assertThat(rs.getString("power_transformer_info_mrid"), equalTo("pti1"))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getString("vector_group"), equalTo("DYN1"))
                assertThat(rs.getDouble("transformer_utilisation"), equalTo(0.0))
                assertThat(rs.getString("construction_kind"), equalTo("unknown"))
                assertThat(rs.getString("function"), equalTo("other"))
                assertThat(rs.getString("power_transformer_info_mrid"), equalTo("pti1"))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM linear_shunt_compensators",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getBoolean("control_enabled"), equalTo(true))
                assertThat(rs.getString("shunt_compensator_info_mrid"), equalTo("s1"))
                assertThat(rs.getBoolean("grounded"), equalTo(true))
                assertThat(rs.getInt("nom_u"), equalTo(30))
                assertThat(rs.getString("phase_connection"), equalTo("D"))
                assertThat(rs.getDouble("sections"), equalTo(1.1))
                assertThat(rs.getDouble("b0_per_section"), equalTo(1.2))
                assertThat(rs.getDouble("b_per_section"), equalTo(1.3))
                assertThat(rs.getDouble("g0_per_section"), equalTo(1.4))
                assertThat(rs.getDouble("g_per_section"), equalTo(1.5))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
                assertThat("regulating_control_mrid should default to NULL", rs.apply { getString("regulating_control_mrid") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getBoolean("control_enabled"), equalTo(true))
                assertThat(rs.getString("shunt_compensator_info_mrid"), equalTo("s1"))
                assertThat(rs.getBoolean("grounded"), equalTo(true))
                assertThat(rs.getInt("nom_u"), equalTo(30))
                assertThat(rs.getString("phase_connection"), equalTo("D"))
                assertThat(rs.getDouble("sections"), equalTo(1.1))
                assertThat(rs.getDouble("b0_per_section"), equalTo(1.2))
                assertThat(rs.getDouble("b_per_section"), equalTo(1.3))
                assertThat(rs.getDouble("g0_per_section"), equalTo(1.4))
                assertThat(rs.getDouble("g_per_section"), equalTo(1.5))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
                assertThat(rs.getString("regulating_control_mrid"), equalTo("rc1"))
            }
        )

        validateRows(statement, "SELECT * FROM energy_consumers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("customer_count"), equalTo(0))
                assertThat(rs.getBoolean("grounded"), equalTo(true))
                assertThat(rs.getDouble("p"), equalTo(0.1))
                assertThat(rs.getDouble("q"), equalTo(0.2))
                assertThat(rs.getDouble("p_fixed"), equalTo(0.3))
                assertThat(rs.getDouble("q_fixed"), equalTo(0.4))
                assertThat(rs.getString("phase_connection"), equalTo("D"))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("customer_count"), equalTo(0))
                assertThat(rs.getBoolean("grounded"), equalTo(true))
                assertThat(rs.getDouble("p"), equalTo(0.1))
                assertThat(rs.getDouble("q"), equalTo(0.2))
                assertThat(rs.getDouble("p_fixed"), equalTo(0.3))
                assertThat(rs.getDouble("q_fixed"), equalTo(0.4))
                assertThat(rs.getString("phase_connection"), equalTo("D"))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM energy_sources",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getDouble(10), equalTo(1.0))
                assertThat(rs.getDouble(11), equalTo(2.0))
                assertThat(rs.getDouble(12), equalTo(3.0))
                assertThat(rs.getDouble(13), equalTo(4.0))
                assertThat(rs.getDouble(14), equalTo(5.0))
                assertThat(rs.getDouble(15), equalTo(6.0))
                assertThat(rs.getDouble(16), equalTo(7.0))
                assertThat(rs.getDouble(17), equalTo(8.0))
                assertThat(rs.getDouble(18), equalTo(9.0))
                assertThat(rs.getDouble(19), equalTo(10.0))
                assertThat(rs.getDouble(20), equalTo(11.0))
                assertThat(rs.getDouble(21), equalTo(12.0))
                assertThat(rs.getBoolean(22), equalTo(true))
                assertThat(rs.getDouble(23), equalTo(13.0))
                assertThat(rs.getDouble(24), equalTo(14.0))
                assertThat(rs.getDouble(25), equalTo(15.0))
                assertThat(rs.getDouble(26), equalTo(16.0))
                assertThat(rs.getDouble(27), equalTo(17.0))
                assertThat(rs.getDouble(28), equalTo(18.0))
                assertThat(rs.getDouble(29), equalTo(19.0))
                assertThat(rs.getDouble(30), equalTo(20.0))
                assertThat(rs.getDouble(31), equalTo(21.0))
                assertThat(rs.getDouble(32), equalTo(22.0))
                assertThat(rs.getDouble(33), equalTo(23.0))
                assertThat(rs.getDouble(34), equalTo(24.0))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getDouble(10), equalTo(1.0))
                assertThat(rs.getDouble(11), equalTo(2.0))
                assertThat(rs.getDouble(12), equalTo(3.0))
                assertThat(rs.getDouble(13), equalTo(4.0))
                assertThat(rs.getDouble(14), equalTo(5.0))
                assertThat(rs.getDouble(15), equalTo(6.0))
                assertThat(rs.getDouble(16), equalTo(7.0))
                assertThat(rs.getDouble(17), equalTo(8.0))
                assertThat(rs.getDouble(18), equalTo(9.0))
                assertThat(rs.getDouble(19), equalTo(10.0))
                assertThat(rs.getDouble(20), equalTo(11.0))
                assertThat(rs.getDouble(21), equalTo(12.0))
                assertThat(rs.getBoolean(22), equalTo(true))
                assertThat(rs.getDouble(23), equalTo(13.0))
                assertThat(rs.getDouble(24), equalTo(14.0))
                assertThat(rs.getDouble(25), equalTo(15.0))
                assertThat(rs.getDouble(26), equalTo(16.0))
                assertThat(rs.getDouble(27), equalTo(17.0))
                assertThat(rs.getDouble(28), equalTo(18.0))
                assertThat(rs.getDouble(29), equalTo(19.0))
                assertThat(rs.getDouble(30), equalTo(20.0))
                assertThat(rs.getDouble(31), equalTo(21.0))
                assertThat(rs.getDouble(32), equalTo(22.0))
                assertThat(rs.getDouble(33), equalTo(23.0))
                assertThat(rs.getDouble(34), equalTo(24.0))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM junctions",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM busbar_sections",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM equivalent_branches",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getDouble(10), equalTo(1.0))
                assertThat(rs.getDouble(11), equalTo(2.0))
                assertThat(rs.getDouble(12), equalTo(3.0))
                assertThat(rs.getDouble(13), equalTo(4.0))
                assertThat(rs.getDouble(14), equalTo(5.0))
                assertThat(rs.getDouble(15), equalTo(6.0))
                assertThat(rs.getDouble(16), equalTo(7.0))
                assertThat(rs.getDouble(17), equalTo(8.0))
                assertThat(rs.getDouble(18), equalTo(9.0))
                assertThat(rs.getDouble(19), equalTo(10.0))
                assertThat(rs.getDouble(20), equalTo(11.0))
                assertThat(rs.getDouble(21), equalTo(12.0))
                assertThat(rs.getDouble(22), equalTo(13.0))
                assertThat(rs.getDouble(23), equalTo(14.0))
                assertThat(rs.getDouble(24), equalTo(15.0))
                assertThat(rs.getDouble(25), equalTo(16.0))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getDouble(10), equalTo(1.0))
                assertThat(rs.getDouble(11), equalTo(2.0))
                assertThat(rs.getDouble(12), equalTo(3.0))
                assertThat(rs.getDouble(13), equalTo(4.0))
                assertThat(rs.getDouble(14), equalTo(5.0))
                assertThat(rs.getDouble(15), equalTo(6.0))
                assertThat(rs.getDouble(16), equalTo(7.0))
                assertThat(rs.getDouble(17), equalTo(8.0))
                assertThat(rs.getDouble(18), equalTo(9.0))
                assertThat(rs.getDouble(19), equalTo(10.0))
                assertThat(rs.getDouble(20), equalTo(11.0))
                assertThat(rs.getDouble(21), equalTo(12.0))
                assertThat(rs.getDouble(22), equalTo(13.0))
                assertThat(rs.getDouble(23), equalTo(14.0))
                assertThat(rs.getDouble(24), equalTo(15.0))
                assertThat(rs.getDouble(25), equalTo(16.0))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM usage_points",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getBoolean("is_virtual"), equalTo(true))
                assertThat(rs.getString("connection_category"), equalTo("test"))
                assertThat("rated_power should default to NULL", rs.apply { getInt("rated_power") }.wasNull())
                assertThat("approved_inverter_capacity should default to NULL", rs.apply { getInt("approved_inverter_capacity") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getBoolean("is_virtual"), equalTo(true))
                assertThat(rs.getString("connection_category"), equalTo("test"))
                assertThat(rs.getInt("rated_power"), equalTo(50))
                assertThat(rs.getInt("approved_inverter_capacity"), equalTo(500))
            }
        )

        validateRows(statement, "SELECT * FROM current_relays",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getDouble("relay_delay_time"), equalTo(2.3))
                assertThat(rs.getString("protection_kind"), equalTo("EF"))
                assertThat(rs.getDouble("current_limit_1"), equalTo(50.0))
                assertThat(rs.getNullableBoolean("inverse_time_flag"), equalTo(null))
                assertThat(rs.getDouble("time_delay_1"), equalTo(1.3))
                assertThat(rs.getString("current_relay_info_mrid"), equalTo("cri1"))
                assertThat("directable should default to NULL", rs.apply { getNullableBoolean("directable") }.wasNull())
                assertThat("power_direction should default to UNKNOWN_DIRECTION", rs.getString("power_direction"), equalTo("UNKNOWN_DIRECTION"))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getDouble("relay_delay_time"), equalTo(2.3))
                assertThat(rs.getString("protection_kind"), equalTo("EF"))
                assertThat(rs.getDouble("current_limit_1"), equalTo(50.0))
                assertThat(rs.getNullableBoolean("inverse_time_flag"), equalTo(null))
                assertThat(rs.getDouble("time_delay_1"), equalTo(1.3))
                assertThat(rs.getString("current_relay_info_mrid"), equalTo("cri1"))
                assertThat(rs.getNullableBoolean("directable"), equalTo(true))
                assertThat(rs.getString("power_direction"), equalTo("FORWARD"))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM disconnectors",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM jumpers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM fuses",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM load_break_switches",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getInt("breaking_capacity"), equalTo(4))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getInt("breaking_capacity"), equalTo(4))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM breakers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getInt("breaking_capacity"), equalTo(4))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getInt("breaking_capacity"), equalTo(4))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM reclosers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getInt("breaking_capacity"), equalTo(4))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getInt("normal_open"), equalTo(1))
                assertThat(rs.getInt("open"), equalTo(2))
                assertThat(rs.getString("switch_info_mrid"), equalTo("swi"))
                assertThat(rs.getInt("rated_current"), equalTo(3))
                assertThat(rs.getInt("breaking_capacity"), equalTo(4))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM photo_voltaic_unit",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("power_electronics_connection_mrid"), equalTo("pec1"))
                assertThat(rs.getDouble("max_p"), equalTo(1.0))
                assertThat(rs.getDouble("min_p"), equalTo(2.0))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("power_electronics_connection_mrid"), equalTo("pec1"))
                assertThat(rs.getDouble("max_p"), equalTo(1.0))
                assertThat(rs.getDouble("min_p"), equalTo(2.0))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM power_electronics_wind_unit",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("power_electronics_connection_mrid"), equalTo("pec1"))
                assertThat(rs.getDouble("max_p"), equalTo(1.0))
                assertThat(rs.getDouble("min_p"), equalTo(2.0))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("power_electronics_connection_mrid"), equalTo("pec1"))
                assertThat(rs.getDouble("max_p"), equalTo(1.0))
                assertThat(rs.getDouble("min_p"), equalTo(2.0))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM battery_unit",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("power_electronics_connection_mrid"), equalTo("pec1"))
                assertThat(rs.getDouble("max_p"), equalTo(1.0))
                assertThat(rs.getDouble("min_p"), equalTo(2.0))
                assertThat(rs.getInt("rated_e"), equalTo(3))
                assertThat(rs.getInt("stored_e"), equalTo(4))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("power_electronics_connection_mrid"), equalTo("pec1"))
                assertThat(rs.getDouble("max_p"), equalTo(1.0))
                assertThat(rs.getDouble("min_p"), equalTo(2.0))
                assertThat(rs.getInt("rated_e"), equalTo(3))
                assertThat(rs.getInt("stored_e"), equalTo(4))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM fault_indicators",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("terminal_mrid"), equalTo("terminal1"))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("terminal_mrid"), equalTo("terminal1"))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM current_transformers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("terminal_mrid"), equalTo("terminal1"))
                assertThat(rs.getString("current_transformer_info_mrid"), equalTo("cti"))
                assertThat(rs.getInt("core_burden"), equalTo(1))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("terminal_mrid"), equalTo("terminal1"))
                assertThat(rs.getString("current_transformer_info_mrid"), equalTo("cti"))
                assertThat(rs.getInt("core_burden"), equalTo(1))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM potential_transformers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("terminal_mrid"), equalTo("terminal1"))
                assertThat(rs.getString("potential_transformer_info_mrid"), equalTo("pti"))
                assertThat(rs.getString("type"), equalTo("UNKNOWN"))
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("terminal_mrid"), equalTo("terminal1"))
                assertThat(rs.getString("potential_transformer_info_mrid"), equalTo("pti"))
                assertThat(rs.getString("type"), equalTo("UNKNOWN"))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
            }
        )

        validateRows(statement, "SELECT * FROM power_electronics_connection",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getBoolean("control_enabled"), equalTo(true))
                assertThat(rs.getInt("max_i_fault"), equalTo(1))
                assertThat(rs.getDouble("max_q"), equalTo(2.0))
                assertThat(rs.getDouble("min_q"), equalTo(3.0))
                assertThat(rs.getDouble("p"), equalTo(4.0))
                assertThat(rs.getDouble("q"), equalTo(5.0))
                assertThat(rs.getInt("rated_s"), equalTo(6))
                assertThat(rs.getInt("rated_u"), equalTo(7))
                assertThat("inverter_standard should default to NULL", rs.apply { getString("inverter_standard") }.wasNull())
                assertThat("sustain_op_overvolt_limit should default to NULL", rs.apply { getInt("sustain_op_overvolt_limit") }.wasNull())
                assertThat("stop_at_over_freq should default to NULL", rs.apply { getFloat("stop_at_over_freq") }.wasNull())
                assertThat("stop_at_under_freq should default to NULL", rs.apply { getFloat("stop_at_under_freq") }.wasNull())
                assertThat("inv_volt_watt_resp_mode should default to NULL", rs.apply { getBoolean("inv_volt_watt_resp_mode") }.wasNull())
                assertThat("inv_watt_resp_v1 should default to NULL", rs.apply { getInt("inv_watt_resp_v1") }.wasNull())
                assertThat("inv_watt_resp_v2 should default to NULL", rs.apply { getInt("inv_watt_resp_v2") }.wasNull())
                assertThat("inv_watt_resp_v3 should default to NULL", rs.apply { getInt("inv_watt_resp_v3") }.wasNull())
                assertThat("inv_watt_resp_v4 should default to NULL", rs.apply { getInt("inv_watt_resp_v4") }.wasNull())
                assertThat("inv_watt_resp_p_at_v1 should default to NULL", rs.apply { getFloat("inv_watt_resp_p_at_v1") }.wasNull())
                assertThat("inv_watt_resp_p_at_v2 should default to NULL", rs.apply { getFloat("inv_watt_resp_p_at_v2") }.wasNull())
                assertThat("inv_watt_resp_p_at_v3 should default to NULL", rs.apply { getFloat("inv_watt_resp_p_at_v3") }.wasNull())
                assertThat("inv_watt_resp_p_at_v4 should default to NULL", rs.apply { getFloat("inv_watt_resp_p_at_v4") }.wasNull())
                assertThat("inv_volt_var_resp_mode should default to NULL", rs.apply { getBoolean("inv_volt_var_resp_mode") }.wasNull())
                assertThat("inv_var_resp_v1 should default to NULL", rs.apply { getInt("inv_var_resp_v1") }.wasNull())
                assertThat("inv_var_resp_v2 should default to NULL", rs.apply { getInt("inv_var_resp_v2") }.wasNull())
                assertThat("inv_var_resp_v3 should default to NULL", rs.apply { getInt("inv_var_resp_v3") }.wasNull())
                assertThat("inv_var_resp_v4 should default to NULL", rs.apply { getInt("inv_var_resp_v4") }.wasNull())
                assertThat("inv_var_resp_q_at_v1 should default to NULL", rs.apply { getFloat("inv_var_resp_q_at_v1") }.wasNull())
                assertThat("inv_var_resp_q_at_v2 should default to NULL", rs.apply { getFloat("inv_var_resp_q_at_v2") }.wasNull())
                assertThat("inv_var_resp_q_at_v3 should default to NULL", rs.apply { getFloat("inv_var_resp_q_at_v3") }.wasNull())
                assertThat("inv_var_resp_q_at_v4 should default to NULL", rs.apply { getFloat("inv_var_resp_q_at_v4") }.wasNull())
                assertThat("inv_reactive_power_mode should default to NULL", rs.apply { getBoolean("inv_reactive_power_mode") }.wasNull())
                assertThat("inv_fix_reactive_power should default to NULL", rs.apply { getFloat("inv_fix_reactive_power") }.wasNull())
                assertThat("commissioned_date should default to NULL", rs.apply { getString("commissioned_date") }.wasNull())
                assertThat("regulating_control_mrid should default to NULL", rs.apply { getString("regulating_control_mrid") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("normally_in_service"), equalTo(true))
                assertThat(rs.getBoolean("in_service"), equalTo(true))
                assertThat(rs.getString("base_voltage_mrid"), equalTo("bv1"))
                assertThat(rs.getBoolean("control_enabled"), equalTo(true))
                assertThat(rs.getInt("max_i_fault"), equalTo(1))
                assertThat(rs.getDouble("max_q"), equalTo(2.0))
                assertThat(rs.getDouble("min_q"), equalTo(3.0))
                assertThat(rs.getDouble("p"), equalTo(4.0))
                assertThat(rs.getDouble("q"), equalTo(5.0))
                assertThat(rs.getInt("rated_s"), equalTo(6))
                assertThat(rs.getInt("rated_u"), equalTo(7))
                assertThat(rs.getString("inverter_standard"), equalTo("standard"))
                assertThat(rs.getInt("sustain_op_overvolt_limit"), equalTo(8))
                assertThat(rs.getFloat("stop_at_over_freq"), equalTo(9.0f))
                assertThat(rs.getFloat("stop_at_under_freq"), equalTo(10.0f))
                assertThat(rs.getBoolean("inv_volt_watt_resp_mode"), equalTo(true))
                assertThat(rs.getInt("inv_watt_resp_v1"), equalTo(11))
                assertThat(rs.getInt("inv_watt_resp_v2"), equalTo(12))
                assertThat(rs.getInt("inv_watt_resp_v3"), equalTo(13))
                assertThat(rs.getInt("inv_watt_resp_v4"), equalTo(14))
                assertThat(rs.getFloat("inv_watt_resp_p_at_v1"), equalTo(15.0f))
                assertThat(rs.getFloat("inv_watt_resp_p_at_v2"), equalTo(16.0f))
                assertThat(rs.getFloat("inv_watt_resp_p_at_v3"), equalTo(17.0f))
                assertThat(rs.getFloat("inv_watt_resp_p_at_v4"), equalTo(18.0f))
                assertThat(rs.getBoolean("inv_volt_var_resp_mode"), equalTo(true))
                assertThat(rs.getInt("inv_var_resp_v1"), equalTo(19))
                assertThat(rs.getInt("inv_var_resp_v2"), equalTo(20))
                assertThat(rs.getInt("inv_var_resp_v3"), equalTo(21))
                assertThat(rs.getInt("inv_var_resp_v4"), equalTo(22))
                assertThat(rs.getFloat("inv_var_resp_q_at_v1"), equalTo(23.0f))
                assertThat(rs.getFloat("inv_var_resp_q_at_v2"), equalTo(24.0f))
                assertThat(rs.getFloat("inv_var_resp_q_at_v3"), equalTo(25.0f))
                assertThat(rs.getFloat("inv_var_resp_q_at_v4"), equalTo(26.0f))
                assertThat(rs.getBoolean("inv_reactive_power_mode"), equalTo(true))
                assertThat(rs.getFloat("inv_fix_reactive_power"), equalTo(27.0f))
                assertThat(rs.getString("commissioned_date"), equalTo("2020-01-01"))
                assertThat(rs.getString("regulating_control_mrid"), equalTo("rc1"))
            }
        )

        validateRows(statement, "SELECT * FROM ratio_tap_changers",
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id2"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("control_enabled"), equalTo(true))
                assertThat(rs.getInt("high_step"), equalTo(1))
                assertThat(rs.getInt("low_step"), equalTo(2))
                assertThat(rs.getInt("neutral_step"), equalTo(3))
                assertThat(rs.getInt("neutral_u"), equalTo(4))
                assertThat(rs.getInt("normal_step"), equalTo(5))
                assertThat(rs.getDouble("step"), equalTo(6.0))
                assertThat(rs.getString("transformer_end_mrid"), equalTo("te2"))
                assertThat(rs.getDouble("step_voltage_increment"), equalTo(7.0))
                assertThat("tap_changer_control_mrid should default to NULL", rs.apply { getString("tap_changer_control_mrid") }.wasNull())
            },
            { rs ->
                assertThat(rs.getString("mrid"), equalTo("id1"))
                assertThat(rs.getString("name"), equalTo("name"))
                assertThat(rs.getString("description"), equalTo("desc"))
                assertThat(rs.getInt("num_diagram_objects"), equalTo(0))
                assertThat(rs.getString("location_mrid"), equalTo("l_id"))
                assertThat(rs.getInt("num_controls"), equalTo(0))
                assertThat(rs.getBoolean("control_enabled"), equalTo(true))
                assertThat(rs.getInt("high_step"), equalTo(1))
                assertThat(rs.getInt("low_step"), equalTo(2))
                assertThat(rs.getInt("neutral_step"), equalTo(3))
                assertThat(rs.getInt("neutral_u"), equalTo(4))
                assertThat(rs.getInt("normal_step"), equalTo(5))
                assertThat(rs.getDouble("step"), equalTo(6.0))
                assertThat(rs.getString("transformer_end_mrid"), equalTo("te1"))
                assertThat(rs.getDouble("step_voltage_increment"), equalTo(7.0))
                assertThat(rs.getString("tap_changer_control_mrid"), equalTo("tc1"))
            }
        )


    }

    override fun tearDownStatements(): List<String> = listOf(
        "DELETE FROM ac_line_segments",
        "DELETE FROM power_transformers",
        "DELETE FROM linear_shunt_compensators",
        "DELETE FROM energy_consumers",
        "DELETE FROM energy_sources",
        "DELETE FROM junctions",
        "DELETE FROM busbar_sections",
        "DELETE FROM disconnectors",
        "DELETE FROM jumpers",
        "DELETE FROM fuses",
        "DELETE FROM load_break_switches",
        "DELETE FROM breakers",
        "DELETE FROM reclosers",
        "DELETE FROM equivalent_branches",
        "DELETE FROM photo_voltaic_unit",
        "DELETE FROM power_electronics_wind_unit",
        "DELETE FROM battery_unit",
        "DELETE FROM current_relays",
        "DELETE FROM fault_indicators",
        "DELETE FROM current_transformers",
        "DELETE FROM potential_transformers",
        "DELETE FROM power_electronics_connection",
        "DELETE FROM linear_shunt_compensators",
        "DELETE FROM ratio_tap_changers",
        "DELETE FROM usage_points",
        "DELETE FROM tap_changer_controls",
        "DELETE FROM ev_charging_units",
        "DELETE FROM reclose_delays",
        "DELETE FROM power_transformer_end_ratings",
    )

}
