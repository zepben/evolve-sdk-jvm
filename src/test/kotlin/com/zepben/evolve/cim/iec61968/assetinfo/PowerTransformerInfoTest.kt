/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.cim.iec61968.assetinfo

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.spy
import com.zepben.evolve.services.network.ResistanceReactance
import com.zepben.evolve.services.network.ResistanceReactanceTest
import com.zepben.evolve.utils.PrivateCollectionValidator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test

internal class PowerTransformerInfoTest {

    @Test
    internal fun constructorCoverage() {
        assertThat(PowerTransformerInfo().mRID, not(equalTo("")))
        assertThat(PowerTransformerInfo("id").mRID, equalTo("id"))
    }

    @Test
    internal fun transformerTankInfo() {
        PrivateCollectionValidator.validate(
            { PowerTransformerInfo() },
            { id, info -> TransformerTankInfo(id).apply { powerTransformerInfo = info } },
            PowerTransformerInfo::numTransformerTankInfos,
            PowerTransformerInfo::getTransformerTankInfo,
            PowerTransformerInfo::transformerTankInfos,
            PowerTransformerInfo::addTransformerTankInfo,
            PowerTransformerInfo::removeTransformerTankInfo,
            PowerTransformerInfo::clearTransformerTankInfos
        )
    }

    @Test
    internal fun populatesResistanceReactanceFromEndsWithMatchingNumber() {
        val tank1 = spy(TransformerTankInfo())
        val tank2 = spy(TransformerTankInfo())
        val txInfo = PowerTransformerInfo().apply {
            addTransformerTankInfo(tank1)
            addTransformerTankInfo(tank2)
        }

        doReturn(ResistanceReactance(1.1, 1.2, 1.3, 1.4)).`when`(tank1).resistanceReactance(1)
        doReturn(ResistanceReactance(2.1, 2.2, 2.3, 2.4)).`when`(tank2).resistanceReactance(2)

        ResistanceReactanceTest.validateResistanceReactance(txInfo.resistanceReactance(1)!!, 1.1, 1.2, 1.3, 1.4)
        ResistanceReactanceTest.validateResistanceReactance(txInfo.resistanceReactance(2)!!, 2.1, 2.2, 2.3, 2.4)
        assertThat(txInfo.resistanceReactance(3), Matchers.nullValue())
    }

}
