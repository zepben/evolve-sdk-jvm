/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.cim.iec61970.base.wires

import org.mockito.kotlin.*
import com.zepben.evolve.cim.iec61968.assetinfo.PowerTransformerInfo
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.ResistanceReactance
import com.zepben.evolve.services.network.ResistanceReactanceTest.Companion.validateResistanceReactance
import com.zepben.evolve.services.network.testdata.fillFields
import com.zepben.testutils.exception.ExpectException
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.times

internal class PowerTransformerEndTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val si = mock<TransformerStarImpedance>()
    private val info = mock<PowerTransformerInfo>()
    private val tx = mock<PowerTransformer>()

    @Test
    internal fun constructorCoverage() {
        assertThat(PowerTransformerEnd().mRID, not(equalTo("")))
        assertThat(PowerTransformerEnd("id").mRID, equalTo("id"))
    }

    @Test
    internal fun accessorCoverage() {
        val powerTransformerEnd = PowerTransformerEnd()

        assertThat(powerTransformerEnd.powerTransformer, nullValue())
        assertThat(powerTransformerEnd.b, nullValue())
        assertThat(powerTransformerEnd.b0, nullValue())
        assertThat(powerTransformerEnd.connectionKind, equalTo(WindingConnection.UNKNOWN_WINDING))
        assertThat(powerTransformerEnd.g, nullValue())
        assertThat(powerTransformerEnd.g0, nullValue())
        assertThat(powerTransformerEnd.phaseAngleClock, nullValue())
        assertThat(powerTransformerEnd.r, nullValue())
        assertThat(powerTransformerEnd.r0, nullValue())
        assertThat(powerTransformerEnd.ratedS, nullValue())
        assertThat(powerTransformerEnd.ratedU, nullValue())
        assertThat(powerTransformerEnd.x, nullValue())
        assertThat(powerTransformerEnd.x0, nullValue())

        powerTransformerEnd.fillFields(NetworkService())

        assertThat(powerTransformerEnd.powerTransformer, notNullValue())
        assertThat(powerTransformerEnd.b, equalTo(1.0))
        assertThat(powerTransformerEnd.b0, equalTo(2.0))
        assertThat(powerTransformerEnd.connectionKind, equalTo(WindingConnection.Zn))
        assertThat(powerTransformerEnd.g, equalTo(3.0))
        assertThat(powerTransformerEnd.g0, equalTo(4.0))
        assertThat(powerTransformerEnd.phaseAngleClock, equalTo(5))
        assertThat(powerTransformerEnd.r, equalTo(6.0))
        assertThat(powerTransformerEnd.r0, equalTo(7.0))
        assertThat(powerTransformerEnd.ratedS, equalTo(8))
        assertThat(powerTransformerEnd.ratedU, equalTo(9))
        assertThat(powerTransformerEnd.x, equalTo(10.0))
        assertThat(powerTransformerEnd.x0, equalTo(11.0))
    }

    @Test
    internal fun `cant assign star impedance when powerTransformer has an AssetInfo`() {
        val tx = PowerTransformer().apply { assetInfo = PowerTransformerInfo() }
        val end = PowerTransformerEnd().apply { powerTransformer = tx }.also { tx.addEnd(it) }

        ExpectException.expect { end.starImpedance = TransformerStarImpedance() }
            .toThrow(IllegalArgumentException::class.java)
            .withMessage("Unable to use a star impedance for ${end.typeNameAndMRID()} directly because ${tx.typeNameAndMRID()} references ${tx.assetInfo?.typeNameAndMRID()}.")
    }

    @Test
    internal fun `only checks for AssetInfo assigned with non-null star impedance`() {
        val tx = PowerTransformer().apply { assetInfo = PowerTransformerInfo() }
        val end = PowerTransformerEnd().apply { powerTransformer = tx }.also { tx.addEnd(it) }
        end.starImpedance = null
    }

    @Test
    internal fun populatesResistanceReactanceDirectlyIfAvailable() {
        val end = PowerTransformerEnd().apply {
            r = 1.1
            r0 = 1.2
            x = 1.3
            x0 = 1.4
        }

        validateResistanceReactance(end.resistanceReactance(), 1.1, 1.2, 1.3, 1.4)
    }

    @Test
    internal fun populatesResistanceReactanceOffStarImpedanceIfAvailable() {
        val end = PowerTransformerEnd().apply {
            powerTransformer = tx
            starImpedance = si
        }
        clearMockitoInvocations()

        doReturn(ResistanceReactance(2.1, 2.2, 2.3, 2.4)).`when`(si).resistanceReactance()

        validateResistanceReactance(end.resistanceReactance(), 2.1, 2.2, 2.3, 2.4)
        verify(si, times(1)).resistanceReactance()
        verify(tx, never()).assetInfo
    }

    @Test
    internal fun populatesResistanceReactanceOffAssetInfoIfAvailable() {
        val end = PowerTransformerEnd().apply {
            endNumber = 123
            powerTransformer = tx
        }

        clearMockitoInvocations()
        doReturn(info).`when`(tx).assetInfo
        doReturn(ResistanceReactance(3.1, 3.2, 3.3, 3.4)).`when`(info).resistanceReactance(end.endNumber)

        validateResistanceReactance(end.resistanceReactance(), 3.1, 3.2, 3.3, 3.4)
        verify(tx, times(1)).assetInfo
        verify(info, times(1)).resistanceReactance(end.endNumber)
    }

    @Test
    internal fun leavesResistanceReactanceUnpopulatedIfNoSourceAvailable() {
        // Isolated end
        val end = PowerTransformerEnd()
        validateResistanceReactance(end.resistanceReactance(), null, null, null, null)

        // With invalid star impedance
        end.starImpedance = si
        validateResistanceReactance(end.resistanceReactance(), null, null, null, null)

        // End on transformer without info
        end.powerTransformer = tx
        validateResistanceReactance(end.resistanceReactance(), null, null, null, null)

        // End on transformer with info but no end info
        doReturn(info).`when`(tx).assetInfo
        validateResistanceReactance(end.resistanceReactance(), null, null, null, null)
    }

    @Test
    internal fun mergesIncompleteWithPrecedence() {
        val end = PowerTransformerEnd().apply {
            r = 1.1
            endNumber = 1
            starImpedance = si
            powerTransformer = tx
        }

        doReturn(info).`when`(tx).assetInfo

        doReturn(ResistanceReactance(null, 2.2, null, null)).`when`(si).resistanceReactance()
        doReturn(ResistanceReactance(null, null, 3.3, null)).`when`(info).resistanceReactance(end.endNumber)

        validateResistanceReactance(end.resistanceReactance(), 1.1, 2.2, 3.3, null)
    }

    private fun clearMockitoInvocations() {
        clearInvocations(si)
        clearInvocations(info)
        clearInvocations(tx)
    }

}
