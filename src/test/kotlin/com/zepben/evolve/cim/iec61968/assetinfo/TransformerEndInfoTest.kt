/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.cim.iec61968.assetinfo

import org.mockito.kotlin.*
import com.zepben.evolve.cim.iec61970.base.wires.TransformerStarImpedance
import com.zepben.evolve.cim.iec61970.base.wires.WindingConnection
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.ResistanceReactance
import com.zepben.evolve.services.network.ResistanceReactanceTest.Companion.validateResistanceReactance
import com.zepben.evolve.services.network.testdata.fillFields
import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.testutils.mockito.DefaultAnswer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class TransformerEndInfoTest {

    @JvmField
    @RegisterExtension
    var systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    @Test
    internal fun constructorCoverage() {
        assertThat(TransformerEndInfo().mRID, not(equalTo("")))
        assertThat(TransformerEndInfo("id").mRID, equalTo("id"))
    }

    @Test
    internal fun accessorCoverage() {
        val transformerEndInfo = TransformerEndInfo()

        assertThat(transformerEndInfo.connectionKind, equalTo(WindingConnection.UNKNOWN_WINDING))
        assertThat(transformerEndInfo.emergencyS, nullValue())
        assertThat(transformerEndInfo.endNumber, equalTo(0))
        assertThat(transformerEndInfo.insulationU, nullValue())
        assertThat(transformerEndInfo.phaseAngleClock, nullValue())
        assertThat(transformerEndInfo.r, nullValue())
        assertThat(transformerEndInfo.ratedS, nullValue())
        assertThat(transformerEndInfo.ratedU, nullValue())
        assertThat(transformerEndInfo.shortTermS, nullValue())
        assertThat(transformerEndInfo.transformerTankInfo, nullValue())
        assertThat(transformerEndInfo.transformerStarImpedance, nullValue())
        assertThat(transformerEndInfo.energisedEndNoLoadTests, nullValue())
        assertThat(transformerEndInfo.energisedEndShortCircuitTests, nullValue())
        assertThat(transformerEndInfo.groundedEndShortCircuitTests, nullValue())
        assertThat(transformerEndInfo.openEndOpenCircuitTests, nullValue())
        assertThat(transformerEndInfo.energisedEndOpenCircuitTests, nullValue())

        transformerEndInfo.fillFields(NetworkService())

        assertThat(transformerEndInfo.connectionKind, equalTo(WindingConnection.D))
        assertThat(transformerEndInfo.emergencyS, equalTo(1))
        assertThat(transformerEndInfo.endNumber, equalTo(2))
        assertThat(transformerEndInfo.insulationU, equalTo(3))
        assertThat(transformerEndInfo.phaseAngleClock, equalTo(4))
        assertThat(transformerEndInfo.r, equalTo(5.0))
        assertThat(transformerEndInfo.ratedS, equalTo(6))
        assertThat(transformerEndInfo.ratedU, equalTo(7))
        assertThat(transformerEndInfo.shortTermS, equalTo(8))
        assertThat(transformerEndInfo.transformerTankInfo, notNullValue())
        assertThat(transformerEndInfo.transformerStarImpedance, notNullValue())
        assertThat(transformerEndInfo.energisedEndNoLoadTests, notNullValue())
        assertThat(transformerEndInfo.energisedEndShortCircuitTests, notNullValue())
        assertThat(transformerEndInfo.groundedEndShortCircuitTests, notNullValue())
        assertThat(transformerEndInfo.openEndOpenCircuitTests, notNullValue())
        assertThat(transformerEndInfo.energisedEndOpenCircuitTests, notNullValue())
    }

    @Test
    internal fun populatesResistanceReactanceOffEndStarImpedanceIfAvailable() {
        val info = spy(TransformerEndInfo().apply {
            transformerStarImpedance = TransformerStarImpedance().apply {
                r = 1.1
                r0 = 1.2
                x = 1.3
                x0 = 1.4
            }
        })

        validateResistanceReactance(info.resistanceReactance()!!, 1.1, 1.2, 1.3, 1.4)
        verify(info, never()).calculateResistanceReactanceFromTests()
    }

    @Test
    internal fun populatesResistanceReactanceOffEndInfoTestsIfAvailable() {
        val info = spy(TransformerEndInfo())
        doReturn(ResistanceReactance(2.1, 2.2, 2.3, 2.4)).`when`(info).calculateResistanceReactanceFromTests()

        validateResistanceReactance(info.resistanceReactance()!!, 2.1, 2.2, 2.3, 2.4)
        verify(info, times(1)).calculateResistanceReactanceFromTests()
    }

    @Test
    internal fun mergesResistanceReactanceIfRequired() {
        val info = spy(TransformerEndInfo().apply {
            transformerStarImpedance =
                mock(defaultAnswer = DefaultAnswer.of(ResistanceReactance::class.java, ResistanceReactance(1.1, null, null, null)))
        })
        doReturn(ResistanceReactance(null, 2.2, null, null)).`when`(info).calculateResistanceReactanceFromTests()

        validateResistanceReactance(info.resistanceReactance()!!, 1.1, 2.2, null, null)
        verify(info, times(1)).calculateResistanceReactanceFromTests()
    }

    @Test
    internal fun calculatesResistanceReactanceOffEndInfoTestsIfAvailable() {
        val info = TransformerEndInfo()

        // https://app.clickup.com/t/6929263/EWB-615 Add checks for calculating off tests
        assertThat(info.calculateResistanceReactanceFromTests(), nullValue())
    }

}
