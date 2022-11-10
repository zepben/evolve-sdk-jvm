/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.cim.iec61970.base.core

import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.utils.PrivateCollectionValidator
import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class ConductingEquipmentTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    @Test
    internal fun constructorCoverage() {
        assertThat(object : ConductingEquipment() {}.mRID, not(equalTo("")))
        assertThat(object : ConductingEquipment("id") {}.mRID, equalTo("id"))
    }

    @Test
    internal fun accessorCoverage() {
        val conductingEquipment = object : ConductingEquipment() {}
        val baseVoltage = BaseVoltage().apply { nominalVoltage = 100 }

        assertThat(conductingEquipment.baseVoltage, nullValue())
        assertThat(conductingEquipment.baseVoltageValue, equalTo(0))

        conductingEquipment.baseVoltage = baseVoltage

        assertThat(conductingEquipment.baseVoltage, equalTo(baseVoltage))
        assertThat(conductingEquipment.baseVoltageValue, equalTo(100))
    }

    @Test
    internal fun assignsEquipmentToTerminalIfMissing() {
        val conductingEquipment = object : ConductingEquipment() {}
        val terminal = Terminal()

        conductingEquipment.addTerminal(terminal)
        assertThat(terminal.conductingEquipment, equalTo(conductingEquipment))
    }

    @Test
    internal fun rejectsTerminalWithWrongEquipment() {
        val ce1 = object : ConductingEquipment() {}
        val ce2 = object : ConductingEquipment() {}
        val terminal = Terminal().apply { conductingEquipment = ce2 }

        expect { ce1.addTerminal(terminal) }
            .toThrow<IllegalArgumentException>()
            .withMessage("${terminal.typeNameAndMRID()} `conductingEquipment` property references ${ce2.typeNameAndMRID()}, expected ${ce1.typeNameAndMRID()}.")
    }

    @Test
    internal fun terminals() {
        PrivateCollectionValidator.validate(
            { object : ConductingEquipment() {} },
            { id, it, sn -> Terminal(id).apply { conductingEquipment = it; sn?.let { sequenceNumber = it } } },
            ConductingEquipment::numTerminals,
            { ce, mRID -> ce.getTerminal(mRID) },
            { ce, seq -> ce.getTerminal(seq) },
            ConductingEquipment::terminals,
            ConductingEquipment::addTerminal,
            ConductingEquipment::removeTerminal,
            ConductingEquipment::clearTerminals

        )
    }

    @Test
    internal fun `test addTerminal sequence numbers`() {
        val ce = object : ConductingEquipment() {}
        val t1 = Terminal()
        val t2 = Terminal()
        val t3 = Terminal().apply { sequenceNumber = 4 }

        // Test order
        ce.addTerminal(t1)
        ce.addTerminal(t2)
        assertThat(ce.terminals, containsInRelativeOrder(t1, t2))

        // Test order maintained and sequenceNumber was set appropriately
        ce.addTerminal(t3)
        assertThat(ce.terminals, containsInRelativeOrder(t1, t2, t3))
        assertThat(ce.getTerminal(1), equalTo(t1))
        assertThat(ce.getTerminal(2), equalTo(t2))
        assertThat(ce.getTerminal(4), equalTo(t3))

        // Test add terminal with same sequence number fails
        val duplicateTerminal = Terminal().apply { sequenceNumber = 1 }
        expect {
            ce.addTerminal(duplicateTerminal)
        }.toThrow<IllegalArgumentException>()
            .withMessage("Unable to add ${duplicateTerminal.typeNameAndMRID()} to ${ce.typeNameAndMRID()}. A ${t1.typeNameAndMRID()} already exists with sequenceNumber 1.")
    }

}
