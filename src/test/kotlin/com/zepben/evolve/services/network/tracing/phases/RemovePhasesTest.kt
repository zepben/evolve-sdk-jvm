/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network.tracing.phases

import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.PhaseCode
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.testdata.SingleLineSinglePhaseNetwork
import com.zepben.evolve.services.network.testdata.SingleLineSwerNetwork
import com.zepben.evolve.services.network.testdata.SinglePhaseJunctionNetwork
import com.zepben.evolve.services.network.testdata.UngangedSwitchLongNetwork
import com.zepben.evolve.services.network.tracing.Tracing
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RemovePhasesTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    @Test
    internal fun removesAllCoreByDefault() {
        val n = UngangedSwitchLongNetwork.create()

        Tracing.setPhases().run(n)
        PhaseLogger.trace(n.get<ConductingEquipment>("node0"))
        validateDirectionsAll(n, PhaseDirection.OUT, PhaseDirection.BOTH, PhaseDirection.IN, PhaseDirection.BOTH)

        Tracing.removePhases().run(n.get<ConductingEquipment>("node1")!!)
        PhaseLogger.trace(n.get<ConductingEquipment>("node0"))
        validateDirectionsAll(n, PhaseDirection.NONE, PhaseDirection.OUT, PhaseDirection.NONE, PhaseDirection.IN)

        Tracing.setPhases().run(n.get<ConductingEquipment>("node1")!!, n.listOf())
        PhaseLogger.trace(n.get<ConductingEquipment>("node0"))
        validateDirectionsAll(n, PhaseDirection.OUT, PhaseDirection.BOTH, PhaseDirection.IN, PhaseDirection.BOTH)
    }

    @Test
    internal fun canRemoveOnlySelectedCores() {
        val n = UngangedSwitchLongNetwork.create()

        Tracing.setPhases().run(n)
        PhaseLogger.trace(n.get<ConductingEquipment>("node0"))
        validateDirectionsSelected(n, PhaseDirection.OUT, PhaseDirection.BOTH, PhaseDirection.IN, PhaseDirection.BOTH)

        Tracing.removePhases().run(n.get<ConductingEquipment>("node1")!!, PhaseCode.AB.singlePhases().toSet())
        PhaseLogger.trace(n.get<ConductingEquipment>("node0"))
        validateDirectionsSelected(n, PhaseDirection.NONE, PhaseDirection.OUT, PhaseDirection.NONE, PhaseDirection.IN)
    }

    @Test
    internal fun removePhasesFromSwer() {
        val n = SingleLineSwerNetwork.create()

        Tracing.setPhases().run(n)
        PhaseLogger.trace(n.get<ConductingEquipment>("source"))
        validateDirection(n, "source", 1, listOf(PhaseDirection.OUT, PhaseDirection.OUT, PhaseDirection.OUT))
        validateDirection(n, "line1", 1, listOf(PhaseDirection.IN, PhaseDirection.IN, PhaseDirection.IN))
        validateDirection(n, "line1", 2, listOf(PhaseDirection.OUT, PhaseDirection.OUT, PhaseDirection.OUT))
        validateDirection(n, "isoTx", 1, listOf(PhaseDirection.IN, PhaseDirection.IN, PhaseDirection.IN))
        validateDirection(n, "isoTx", 2, listOf(PhaseDirection.OUT, PhaseDirection.OUT, PhaseDirection.OUT))
        validateDirection(n, "line2", 1, listOf(PhaseDirection.IN))
        validateDirection(n, "line2", 2, listOf(PhaseDirection.OUT))

        Tracing.removePhases().run(n.get<ConductingEquipment>("line1")!!)
        PhaseLogger.trace(n.get<ConductingEquipment>("source"))
        validateDirection(n, "source", 1, listOf(PhaseDirection.OUT, PhaseDirection.OUT, PhaseDirection.OUT))
        validateDirection(n, "line1", 1, listOf(PhaseDirection.IN, PhaseDirection.IN, PhaseDirection.IN))
        validateDirection(n, "line1", 2, listOf(PhaseDirection.NONE, PhaseDirection.NONE, PhaseDirection.NONE))
        validateDirection(n, "isoTx", 1, listOf(PhaseDirection.NONE, PhaseDirection.NONE, PhaseDirection.NONE))
        validateDirection(n, "isoTx", 2, listOf(PhaseDirection.NONE, PhaseDirection.NONE, PhaseDirection.NONE))
        validateDirection(n, "line2", 1, listOf(PhaseDirection.NONE))
        validateDirection(n, "line2", 2, listOf(PhaseDirection.NONE))
    }

    @Test
    internal fun removePhasesFromSinglePhase() {
        val n = SingleLineSinglePhaseNetwork.create()

        Tracing.setPhases().run(n)
        PhaseLogger.trace(n.get<ConductingEquipment>("source"))
        validateDirection(n, "source", 1, listOf(PhaseDirection.OUT, PhaseDirection.OUT, PhaseDirection.OUT))
        validateDirection(n, "line1", 1, listOf(PhaseDirection.IN, PhaseDirection.IN, PhaseDirection.IN))
        validateDirection(n, "line1", 2, listOf(PhaseDirection.OUT, PhaseDirection.OUT, PhaseDirection.OUT))
        validateDirection(n, "line2", 1, listOf(PhaseDirection.IN, PhaseDirection.IN))
        validateDirection(n, "line2", 2, listOf(PhaseDirection.OUT, PhaseDirection.OUT))

        Tracing.removePhases().run(n.get<ConductingEquipment>("line1")!!)
        PhaseLogger.trace(n.get<ConductingEquipment>("source"))
        validateDirection(n, "source", 1, listOf(PhaseDirection.OUT, PhaseDirection.OUT, PhaseDirection.OUT))
        validateDirection(n, "line1", 1, listOf(PhaseDirection.IN, PhaseDirection.IN, PhaseDirection.IN))
        validateDirection(n, "line1", 2, listOf(PhaseDirection.NONE, PhaseDirection.NONE, PhaseDirection.NONE))
        validateDirection(n, "line2", 1, listOf(PhaseDirection.NONE, PhaseDirection.NONE))
        validateDirection(n, "line2", 2, listOf(PhaseDirection.NONE, PhaseDirection.NONE))
    }

    @Test
    internal fun respectsMultiFeeds() {
        var n = SinglePhaseJunctionNetwork.create()
        PhaseLogger.trace(n.get<ConductingEquipment>("n1"))

        val both = listOf(PhaseDirection.BOTH)
        val `in` = listOf(PhaseDirection.IN)
        val out = listOf(PhaseDirection.OUT)
        val none = listOf(PhaseDirection.NONE)
        val inAndIn = listOf(PhaseDirection.IN, PhaseDirection.IN)
        val outAndOut = listOf(PhaseDirection.OUT, PhaseDirection.OUT)
        val bothAndIn = listOf(PhaseDirection.BOTH, PhaseDirection.IN)
        val bothAndOut = listOf(PhaseDirection.BOTH, PhaseDirection.OUT)
        val inAndNone = listOf(PhaseDirection.IN, PhaseDirection.NONE)
        val outAndNone = listOf(PhaseDirection.OUT, PhaseDirection.NONE)
        val noneAndNone = listOf(PhaseDirection.NONE, PhaseDirection.NONE)

        Tracing.removePhases().run(n.get<ConductingEquipment>("n1")!!, PhaseCode.AB.singlePhases().toSet())
        PhaseLogger.trace(n.get<ConductingEquipment>("n1"))

        validateDirection(n, "n1", 1, inAndIn)
        validateDirection(n, "n2", 1, bothAndOut)
        validateDirection(n, "n3", 1, both)
        validateDirection(n, "n4", 1, inAndIn)
        validateDirection(n, "n4", 2, outAndOut)
        validateDirection(n, "c1", 1, outAndOut)
        validateDirection(n, "c1", 2, inAndIn)
        validateDirection(n, "c2", 1, bothAndOut)
        validateDirection(n, "c2", 2, bothAndIn)
        validateDirection(n, "c3", 1, both)
        validateDirection(n, "c3", 2, both)
        validateDirection(n, "c4", 1, inAndIn)
        validateDirection(n, "c4", 2, outAndOut)

        Tracing.removePhases().run(n.get<ConductingEquipment>("n2")!!, PhaseCode.AB.singlePhases().toSet())
        PhaseLogger.trace(n.get<ConductingEquipment>("n1"))

        validateDirection(n, "n1", 1, inAndNone)
        validateDirection(n, "n2", 1, inAndNone)
        validateDirection(n, "n3", 1, out)
        validateDirection(n, "n4", 1, inAndNone)
        validateDirection(n, "n4", 2, outAndNone)
        validateDirection(n, "c1", 1, outAndNone)
        validateDirection(n, "c1", 2, inAndNone)
        validateDirection(n, "c2", 1, inAndNone)
        validateDirection(n, "c2", 2, outAndNone)
        validateDirection(n, "c3", 1, `in`)
        validateDirection(n, "c3", 2, out)
        validateDirection(n, "c4", 1, inAndNone)
        validateDirection(n, "c4", 2, outAndNone)

        Tracing.removePhases().run(n.get<ConductingEquipment>("n3")!!, PhaseCode.A.singlePhases().toSet())
        PhaseLogger.trace(n.get<ConductingEquipment>("n1"))

        validateDirection(n, "n1", 1, noneAndNone)
        validateDirection(n, "n2", 1, noneAndNone)
        validateDirection(n, "n3", 1, none)
        validateDirection(n, "n4", 1, noneAndNone)
        validateDirection(n, "n4", 2, noneAndNone)
        validateDirection(n, "c1", 1, noneAndNone)
        validateDirection(n, "c1", 2, noneAndNone)
        validateDirection(n, "c2", 1, noneAndNone)
        validateDirection(n, "c2", 2, noneAndNone)
        validateDirection(n, "c3", 1, none)
        validateDirection(n, "c3", 2, none)
        validateDirection(n, "c4", 1, noneAndNone)
        validateDirection(n, "c4", 2, noneAndNone)

        n = SinglePhaseJunctionNetwork.create()

        Tracing.removePhases().run(n.get<ConductingEquipment>("c1")!!, PhaseCode.AB.singlePhases().toSet())
        PhaseLogger.trace(n.get<ConductingEquipment>("n1"))

        validateDirection(n, "n1", 1, outAndOut)
        validateDirection(n, "n2", 1, bothAndOut)
        validateDirection(n, "n3", 1, both)
        validateDirection(n, "n4", 1, inAndIn)
        validateDirection(n, "n4", 2, outAndOut)
        validateDirection(n, "c1", 1, inAndIn)
        validateDirection(n, "c1", 2, inAndIn)
        validateDirection(n, "c2", 1, bothAndOut)
        validateDirection(n, "c2", 2, bothAndIn)
        validateDirection(n, "c3", 1, both)
        validateDirection(n, "c3", 2, both)
        validateDirection(n, "c4", 1, inAndIn)
        validateDirection(n, "c4", 2, outAndOut)

        Tracing.removePhases().run(n.get<ConductingEquipment>("c2")!!, PhaseCode.AB.singlePhases().toSet())
        PhaseLogger.trace(n.get<ConductingEquipment>("n1"))

        validateDirection(n, "n1", 1, outAndOut)
        validateDirection(n, "n2", 1, outAndOut)
        validateDirection(n, "n3", 1, out)
        validateDirection(n, "n4", 1, inAndNone)
        validateDirection(n, "n4", 2, outAndNone)
        validateDirection(n, "c1", 1, inAndIn)
        validateDirection(n, "c1", 2, inAndNone)
        validateDirection(n, "c2", 1, inAndNone)
        validateDirection(n, "c2", 2, inAndIn)
        validateDirection(n, "c3", 1, `in`)
        validateDirection(n, "c3", 2, out)
        validateDirection(n, "c4", 1, inAndNone)
        validateDirection(n, "c4", 2, outAndNone)

        Tracing.removePhases().run(n.get<ConductingEquipment>("c3")!!, PhaseCode.A.singlePhases().toSet())
        PhaseLogger.trace(n.get<ConductingEquipment>("n1"))

        validateDirection(n, "n1", 1, outAndOut)
        validateDirection(n, "n2", 1, outAndOut)
        validateDirection(n, "n3", 1, out)
        validateDirection(n, "n4", 1, noneAndNone)
        validateDirection(n, "n4", 2, noneAndNone)
        validateDirection(n, "c1", 1, inAndIn)
        validateDirection(n, "c1", 2, noneAndNone)
        validateDirection(n, "c2", 1, noneAndNone)
        validateDirection(n, "c2", 2, inAndIn)
        validateDirection(n, "c3", 1, `in`)
        validateDirection(n, "c3", 2, none)
        validateDirection(n, "c4", 1, noneAndNone)
        validateDirection(n, "c4", 2, noneAndNone)
    }

    private fun validateDirectionsAll(network: NetworkService, d11: PhaseDirection, d12: PhaseDirection, d21: PhaseDirection, d22: PhaseDirection) {
        val d11D22D11D22 = listOf(d11, d22, d11, d22)
        val d21D12D21D12 = listOf(d21, d12, d21, d12)
        val outD12OutD12 = listOf(PhaseDirection.OUT, d12, PhaseDirection.OUT, d12)
        val inD22InD22 = listOf(PhaseDirection.IN, d22, PhaseDirection.IN, d22)

        validateDirection(network, "node0", 1, outD12OutD12)
        validateDirection(network, "acLineSegment0", 1, inD22InD22)
        validateDirection(network, "acLineSegment0", 2, outD12OutD12)
        validateDirection(network, "node1", 1, inD22InD22)
        validateDirection(network, "node1", 2, d11D22D11D22)
        validateDirection(network, "acLineSegment1", 1, d21D12D21D12)
        validateDirection(network, "acLineSegment1", 2, d11D22D11D22)
        validateDirection(network, "node2", 1, d21D12D21D12)
        validateDirection(network, "node2", 2, inD22InD22)
        validateDirection(network, "acLineSegment2", 1, outD12OutD12)
        validateDirection(network, "acLineSegment2", 2, inD22InD22)
        validateDirection(network, "node3", 1, outD12OutD12)
        validateDirection(network, "node3", 2, inD22InD22)
        validateDirection(network, "acLineSegment3", 1, outD12OutD12)
        validateDirection(network, "acLineSegment3", 2, inD22InD22)
        validateDirection(network, "node4", 1, outD12OutD12)
    }

    private fun validateDirectionsSelected(network: NetworkService, d11: PhaseDirection, d12: PhaseDirection, d21: PhaseDirection, d22: PhaseDirection) {
        val d11d22OutBoth = listOf(d11, d22, PhaseDirection.OUT, PhaseDirection.BOTH)
        val d21d12InBoth = listOf(d21, d12, PhaseDirection.IN, PhaseDirection.BOTH)
        val outD12OutBoth = listOf(PhaseDirection.OUT, d12, PhaseDirection.OUT, PhaseDirection.BOTH)
        val inD22inD22 = listOf(PhaseDirection.IN, d22, PhaseDirection.IN, PhaseDirection.BOTH)

        validateDirection(network, "node0", 1, outD12OutBoth)
        validateDirection(network, "acLineSegment0", 1, inD22inD22)
        validateDirection(network, "acLineSegment0", 2, outD12OutBoth)
        validateDirection(network, "node1", 1, inD22inD22)
        validateDirection(network, "node1", 2, d11d22OutBoth)
        validateDirection(network, "acLineSegment1", 1, d21d12InBoth)
        validateDirection(network, "acLineSegment1", 2, d11d22OutBoth)
        validateDirection(network, "node2", 1, d21d12InBoth)
        validateDirection(network, "node2", 2, inD22inD22)
        validateDirection(network, "acLineSegment2", 1, outD12OutBoth)
        validateDirection(network, "acLineSegment2", 2, inD22inD22)
        validateDirection(network, "node3", 1, outD12OutBoth)
        validateDirection(network, "node3", 2, inD22inD22)
        validateDirection(network, "acLineSegment3", 1, outD12OutBoth)
        validateDirection(network, "acLineSegment3", 2, inD22inD22)
        validateDirection(network, "node4", 1, outD12OutBoth)
    }

    private fun validateDirection(network: NetworkService, id: String, terminalNo: Int, expectedDirections: List<PhaseDirection>) {
        val terminal: Terminal = network.get<ConductingEquipment>(id)!!.getTerminal(terminalNo)!!
        for (index in expectedDirections.indices) MatcherAssert.assertThat(
            terminal.normalPhases(terminal.phases.singlePhases()[index]).direction,
            Matchers.equalTo(expectedDirections[index])
        )
    }

}
