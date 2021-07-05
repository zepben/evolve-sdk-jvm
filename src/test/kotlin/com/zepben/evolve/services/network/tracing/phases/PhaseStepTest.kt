/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network.tracing.phases

import org.mockito.kotlin.mock
import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.wires.SinglePhaseKind.*
import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.testutils.mockito.DefaultAnswer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class PhaseStepTest {

    @JvmField
    @RegisterExtension
    var systemOutRule: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    @Test
    fun accessors() {
        val asset = mock<ConductingEquipment>()
        val atoc = PhaseStep.startAt(asset, listOf(A, B, C, N, B, C, N))

        assertThat(atoc.conductingEquipment, equalTo(asset))
        assertThat(atoc.phases, containsInAnyOrder(A, B, C, N))
    }

    @Test
    fun coverage() {
        val asset1 = mock<ConductingEquipment>(defaultAnswer = DefaultAnswer.of(String::class.java, "asset1"))
        val asset2 = mock<ConductingEquipment>(defaultAnswer = DefaultAnswer.of(String::class.java, "asset2"))

        val atoc1 = PhaseStep.startAt(asset1, listOf(A, B, C, N, B, C, N))
        val atoc1Dup = PhaseStep.startAt(asset1, listOf(A, B, C, N, B, C, N))
        val atoc2 = PhaseStep.startAt(asset2, listOf(A, B, C, N, B, C, N))
        val atoc3 = PhaseStep.startAt(asset1, listOf(A, B))

        assertThat(atoc1, equalTo(atoc1))
        assertThat(atoc1, equalTo(atoc1Dup))
        assertThat(atoc1, not(equalTo(null)))
        assertThat(atoc1, not(equalTo(atoc2)))
        assertThat(atoc1, not(equalTo(atoc3)))

        assertThat(atoc1.hashCode(), equalTo(atoc1Dup.hashCode()))

        assertThat(atoc1.toString(), not((emptyString())))
    }

}
