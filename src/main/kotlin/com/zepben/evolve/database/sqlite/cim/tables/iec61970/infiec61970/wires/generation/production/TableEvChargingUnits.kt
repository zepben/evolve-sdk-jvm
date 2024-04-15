/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.tables.iec61970.infiec61970.wires.generation.production

import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires.generation.production.TablePowerElectronicsUnit

class TableEvChargingUnits : TablePowerElectronicsUnit() {

    override val name: String = "ev_charging_units"

}
