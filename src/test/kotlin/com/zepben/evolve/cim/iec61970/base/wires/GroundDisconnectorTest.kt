/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.cim.iec61970.base.wires

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test

internal class GroundDisconnectorTest {

    @Test
    internal fun constructorCoverage() {
        assertThat(GroundDisconnector().mRID, not(equalTo("")))
        assertThat(GroundDisconnector("id").mRID, equalTo("id"))
    }

}
