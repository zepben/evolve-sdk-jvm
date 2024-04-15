/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.network

import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.database.sqlite.cim.network.NetworkCimWriter
import com.zepben.evolve.database.sqlite.cim.network.NetworkServiceWriter
import com.zepben.evolve.services.network.NetworkService
import com.zepben.testutils.junit.SystemLogExtension
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class NetworkServiceWriterTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val networkService = NetworkService()
    private val cimWriter = mockk<NetworkCimWriter> { every { save(any<Circuit>()) } returns true }
    private val networkServiceWriter = NetworkServiceWriter(networkService, mockk(), cimWriter)

    //
    // NOTE: We don't do an exhaustive test of saving objects as this is done via the schema test.
    //

    @Test
    internal fun `passes objects through to the cim writer`() {
        val circuit = Circuit().also { networkService.add(it) }

        // NOTE: the save method will fail due to the relaxed mock returning false for all save operations,
        //       but a save should still be attempted on every object
        networkServiceWriter.save()

        verify(exactly = 1) { cimWriter.save(circuit) }
    }

}
