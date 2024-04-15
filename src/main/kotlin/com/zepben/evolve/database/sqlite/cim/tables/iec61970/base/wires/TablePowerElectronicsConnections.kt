/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires

import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.Column.Nullable.NULL

@Suppress("PropertyName")
class TablePowerElectronicsConnections : TableRegulatingCondEq() {

    val MAX_I_FAULT: Column = Column(++columnIndex, "max_i_fault", "INTEGER", NULL)
    val MAX_Q: Column = Column(++columnIndex, "max_q", "NUMBER", NULL)
    val MIN_Q: Column = Column(++columnIndex, "min_q", "NUMBER", NULL)
    val P: Column = Column(++columnIndex, "p", "NUMBER", NULL)
    val Q: Column = Column(++columnIndex, "q", "NUMBER", NULL)
    val RATED_S: Column = Column(++columnIndex, "rated_s", "INTEGER", NULL)
    val RATED_U: Column = Column(++columnIndex, "rated_u", "INTEGER", NULL)
    val INVERTER_STANDARD: Column = Column(++columnIndex, "inverter_standard", "TEXT", NULL)
    val SUSTAIN_OP_OVERVOLT_LIMIT: Column = Column(++columnIndex, "sustain_op_overvolt_limit", "INTEGER", NULL)
    val STOP_AT_OVER_FREQ: Column = Column(++columnIndex, "stop_at_over_freq", "NUMBER", NULL)
    val STOP_AT_UNDER_FREQ: Column = Column(++columnIndex, "stop_at_under_freq", "NUMBER", NULL)
    val INV_VOLT_WATT_RESP_MODE: Column = Column(++columnIndex, "inv_volt_watt_resp_mode", "BOOLEAN", NULL)
    val INV_WATT_RESP_V1: Column = Column(++columnIndex, "inv_watt_resp_v1", "INTEGER", NULL)
    val INV_WATT_RESP_V2: Column = Column(++columnIndex, "inv_watt_resp_v2", "INTEGER", NULL)
    val INV_WATT_RESP_V3: Column = Column(++columnIndex, "inv_watt_resp_v3", "INTEGER", NULL)
    val INV_WATT_RESP_V4: Column = Column(++columnIndex, "inv_watt_resp_v4", "INTEGER", NULL)
    val INV_WATT_RESP_P_AT_V1: Column = Column(++columnIndex, "inv_watt_resp_p_at_v1", "NUMBER", NULL)
    val INV_WATT_RESP_P_AT_V2: Column = Column(++columnIndex, "inv_watt_resp_p_at_v2", "NUMBER", NULL)
    val INV_WATT_RESP_P_AT_V3: Column = Column(++columnIndex, "inv_watt_resp_p_at_v3", "NUMBER", NULL)
    val INV_WATT_RESP_P_AT_V4: Column = Column(++columnIndex, "inv_watt_resp_p_at_v4", "NUMBER", NULL)
    val INV_VOLT_VAR_RESP_MODE: Column = Column(++columnIndex, "inv_volt_var_resp_mode", "BOOLEAN", NULL)
    val INV_VAR_RESP_V1: Column = Column(++columnIndex, "inv_var_resp_v1", "INTEGER", NULL)
    val INV_VAR_RESP_V2: Column = Column(++columnIndex, "inv_var_resp_v2", "INTEGER", NULL)
    val INV_VAR_RESP_V3: Column = Column(++columnIndex, "inv_var_resp_v3", "INTEGER", NULL)
    val INV_VAR_RESP_V4: Column = Column(++columnIndex, "inv_var_resp_v4", "INTEGER", NULL)
    val INV_VAR_RESP_Q_AT_V1: Column = Column(++columnIndex, "inv_var_resp_q_at_v1", "NUMBER", NULL)
    val INV_VAR_RESP_Q_AT_V2: Column = Column(++columnIndex, "inv_var_resp_q_at_v2", "NUMBER", NULL)
    val INV_VAR_RESP_Q_AT_V3: Column = Column(++columnIndex, "inv_var_resp_q_at_v3", "NUMBER", NULL)
    val INV_VAR_RESP_Q_AT_V4: Column = Column(++columnIndex, "inv_var_resp_q_at_v4", "NUMBER", NULL)
    val INV_REACTIVE_POWER_MODE: Column = Column(++columnIndex, "inv_reactive_power_mode", "BOOLEAN", NULL)
    val INV_FIX_REACTIVE_POWER: Column = Column(++columnIndex, "inv_fix_reactive_power", "NUMBER", NULL)

    override val name: String = "power_electronics_connections"

}
