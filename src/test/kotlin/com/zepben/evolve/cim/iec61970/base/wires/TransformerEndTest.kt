/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.cim.iec61970.base.wires

import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.testdata.fillFields
import com.zepben.testutils.exception.ExpectException
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class TransformerEndTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    @Test
    internal fun constructorCoverage() {
        assertThat(object : TransformerEnd() {}.mRID, not(equalTo("")))
        assertThat(object : TransformerEnd("id") {}.mRID, equalTo("id"))
    }

    @Test
    internal fun accessorCoverage() {
        val transformerEnd = object : TransformerEnd() {}

        assertThat(transformerEnd.grounded, equalTo(false))
        assertThat(transformerEnd.rGround, nullValue())
        assertThat(transformerEnd.xGround, nullValue())
        assertThat(transformerEnd.baseVoltage, nullValue())
        assertThat(transformerEnd.ratioTapChanger, nullValue())
        assertThat(transformerEnd.terminal, nullValue())
        assertThat(transformerEnd.starImpedance, nullValue())

        transformerEnd.fillFields(NetworkService(), true)

        assertThat(transformerEnd.grounded, equalTo(true))
        assertThat(transformerEnd.rGround, equalTo(1.0))
        assertThat(transformerEnd.xGround, equalTo(2.0))
        assertThat(transformerEnd.baseVoltage, notNullValue())
        assertThat(transformerEnd.ratioTapChanger, notNullValue())
        assertThat(transformerEnd.terminal, notNullValue())
        assertThat(transformerEnd.starImpedance, notNullValue())
    }

    @Test
    internal fun throwsOnUnknownEndType() {
        val end = object : TransformerEnd() {}
        ExpectException.expect { end.resistanceReactance() }
            .toThrow(NotImplementedError::class.java)
            .withMessage("Unknown transformer end leaf type: ${end.typeNameAndMRID()}. Add support which should at least include `starImpedance?.resistanceReactance() ?: ResistanceReactance()`.")
    }

}
