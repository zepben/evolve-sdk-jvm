/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.cim.iec61968.assets

/**
 * A Streetlight asset
 */
class Streetlight @JvmOverloads constructor(mRID: String = "") : Asset(mRID) {

    /**
     * The [Pole] this [Streetlight] is attached to.
     */
    var pole: Pole? = null

    /**
     * Power rating of light
     */
    var lightRating: Int? = null

    /**
     * The kind of lamp
     */
    var lampKind: StreetlightLampKind = StreetlightLampKind.UNKNOWN


}
