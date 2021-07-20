/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.cim.iec61968.metering

import com.zepben.evolve.cim.iec61970.base.core.Equipment
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.testdata.fillFields
import com.zepben.evolve.utils.PrivateCollectionValidator
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class UsagePointTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    @Test
    internal fun constructorCoverage() {
        assertThat(UsagePoint().mRID, not(equalTo("")))
        assertThat(UsagePoint("id").mRID, equalTo("id"))
    }

    @Test
    internal fun accessorCoverage() {
        val usagePoint = UsagePoint()

        assertThat(usagePoint.usagePointLocation, nullValue())
        assertThat(usagePoint.isVirtual, equalTo(false))
        assertThat(usagePoint.connectionCategory, nullValue())

        usagePoint.fillFields(NetworkService(), true)

        assertThat(usagePoint.usagePointLocation, notNullValue())
        assertThat(usagePoint.isVirtual, equalTo(true))
        assertThat(usagePoint.connectionCategory, equalTo("connectionCategory"))
    }

    @Test
    internal fun endDevices() {
        PrivateCollectionValidator.validate(
            { UsagePoint() },
            { id, _ -> object : EndDevice(id) {} },
            UsagePoint::numEndDevices,
            UsagePoint::getEndDevice,
            UsagePoint::endDevices,
            UsagePoint::addEndDevice,
            UsagePoint::removeEndDevice,
            UsagePoint::clearEndDevices
        )
    }

    @Test
    internal fun equipment() {
        PrivateCollectionValidator.validate(
            { UsagePoint() },
            { id, _ -> object : Equipment(id) {} },
            UsagePoint::numEquipment,
            UsagePoint::getEquipment,
            UsagePoint::equipment,
            UsagePoint::addEquipment,
            UsagePoint::removeEquipment,
            UsagePoint::clearEquipment
        )
    }
}
