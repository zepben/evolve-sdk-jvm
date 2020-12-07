/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.cimbend.testdata

import com.zepben.cimbend.cim.iec61970.base.core.ConductingEquipment
import com.zepben.cimbend.cim.iec61970.base.core.PhaseCode
import com.zepben.cimbend.cim.iec61970.base.wires.AcLineSegment
import com.zepben.cimbend.cim.iec61970.base.wires.EnergySource
import com.zepben.cimbend.network.NetworkService
import com.zepben.cimbend.network.tracing.Tracing
import com.zepben.cimbend.testdata.TestDataCreators.*


object PhasesTestNetwork {

    fun withSource(phases: PhaseCode): Builder {
        val network = NetworkService()
        val source = createSourceForConnecting(network, "source", 1, phases)
        createTerminals(network, source, 1, phases)
        return Builder(source, network)
    }

    data class Builder(
        private val source: EnergySource,
        val network: NetworkService
    ) {
        private var current: ConductingEquipment
        private var count = 0

        init {
            current = source
        }

        fun connectedTo(phases: PhaseCode): Builder {
            val c: AcLineSegment = createAcLineSegmentForConnecting(network, "c" + count++, phases)
            network.connect(current.getTerminal(if (current is EnergySource) 1 else 2)!!, c.getTerminal(1)!!)
            current = c
            return this
        }

        fun build(): NetworkService {
            Tracing.setPhases().run(network)
            return network
        }

    }

}
