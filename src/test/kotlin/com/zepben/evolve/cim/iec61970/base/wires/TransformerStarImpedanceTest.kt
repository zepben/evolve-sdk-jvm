/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.cim.iec61970.base.wires

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import com.zepben.evolve.cim.iec61968.assetinfo.TransformerEndInfo
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.ResistanceReactance
import com.zepben.evolve.services.network.ResistanceReactanceTest.Companion.validateResistanceReactance
import com.zepben.evolve.services.network.testdata.fillFields
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class TransformerStarImpedanceTest {

    @JvmField
    @RegisterExtension
    var systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    @Test
    internal fun constructorCoverage() {
        assertThat(TransformerStarImpedance().mRID, not(equalTo("")))
        assertThat(TransformerStarImpedance("id").mRID, equalTo("id"))
    }

    @Test
    internal fun accessorCoverage() {
        val transformerStarImpedance = TransformerStarImpedance()

        assertThat(transformerStarImpedance.r, nullValue())
        assertThat(transformerStarImpedance.r0, nullValue())
        assertThat(transformerStarImpedance.x, nullValue())
        assertThat(transformerStarImpedance.x0, nullValue())
        assertThat(transformerStarImpedance.transformerEndInfo, nullValue())

        transformerStarImpedance.fillFields(NetworkService())

        assertThat(transformerStarImpedance.r, equalTo(1.0))
        assertThat(transformerStarImpedance.r0, equalTo(2.0))
        assertThat(transformerStarImpedance.x, equalTo(3.0))
        assertThat(transformerStarImpedance.x0, equalTo(4.0))
        assertThat(transformerStarImpedance.transformerEndInfo, notNullValue())
    }

    @Test
    internal fun populatesResistanceReactanceDirectlyIfAvailable() {
        val rr = TransformerStarImpedance().apply {
            r = 1.1
            r0 = 1.2
            x = 1.3
            x0 = 1.4
        }.resistanceReactance()

        validateResistanceReactance(rr, 1.1, 1.2, 1.3, 1.4)
    }

    @Test
    internal fun calculatesResistanceReactanceOffEndInfoTestsIfAvailable() {
        val info = mock<TransformerEndInfo>()
        doReturn(ResistanceReactance(2.1, 2.2, 2.3, 2.4)).`when`(info).calculateResistanceReactanceFromTests()

        val rr = TransformerStarImpedance().apply { transformerEndInfo = info }
            .resistanceReactance()

        validateResistanceReactance(rr, 2.1, 2.2, 2.3, 2.4)
        verify(info, times(1)).calculateResistanceReactanceFromTests()
    }

    @Test
    internal fun leavesResistanceReactanceUnpopulatedIfNoSourceAvailable() {
        val info = mock<TransformerEndInfo>()

        // Isolated star impedance
        val starImpedance = TransformerStarImpedance()
        validateResistanceReactance(starImpedance.resistanceReactance(), null, null, null, null)

        // End info with no resistance/reactance
        starImpedance.transformerEndInfo = info
        validateResistanceReactance(starImpedance.resistanceReactance(), null, null, null, null)
    }

    @Test
    internal fun mergesResistanceReactanceWhenRequired() {
        val info = mock<TransformerEndInfo>()
        doReturn(ResistanceReactance(null, 2.2, null, null)).`when`(info).calculateResistanceReactanceFromTests()

        val rr = TransformerStarImpedance().apply {
            r = 1.1
            transformerEndInfo = info
        }.resistanceReactance()

        validateResistanceReactance(rr, 1.1, 2.2, null, null)
    }

}
