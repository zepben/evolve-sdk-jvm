/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.cim.iec61970.base.wires

/**
 * A type of conducting equipment that can regulate a quantity (i.e. voltage or flow) at a specific point in the network.
 * @property controlEnabled Specifies the regulation status of the equipment.  True is regulating, false is not regulating.
 */
abstract class RegulatingCondEq(mRID: String = "") : EnergyConnection(mRID) {

    var controlEnabled: Boolean = true
}
