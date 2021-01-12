/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package phasing

import com.zepben.evolve.cim.iec61970.base.core.BaseVoltage
import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.PhaseCode
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.cim.iec61970.base.wires.AcLineSegment
import com.zepben.evolve.cim.iec61970.base.wires.Breaker
import com.zepben.evolve.cim.iec61970.base.wires.PowerTransformer
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.tracing.Tracing
import com.zepben.evolve.services.network.tracing.TracingTest
import com.zepben.evolve.services.network.tracing.phases.PhaseInferrer
import com.zepben.evolve.services.network.tracing.phases.PhaseInferrerTest
import com.zepben.evolve.services.network.tracing.phases.PhaseStep
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.HashSet


internal class TestNetwork1(){

    // Three acls test network:  - acls1(ABC) - acls2(AB) - acls3-(ABC) -

    var service: NetworkService = NetworkService()
    init{
        val acls1 = add_acls(mRID = "acls1",phs = PhaseCode.ABC)
        val acls2 = add_acls(mRID = "acls2",phs = PhaseCode.AB)
        val acls3 = add_acls(mRID = "acls3",phs = PhaseCode.ABC)
        acls1.getTerminal(2)?.let { acls2.getTerminal(1)?.let { it1 -> service.connect(it, it1) } }
        acls2.getTerminal(2)?.let { acls3.getTerminal(1)?.let { it1 -> service.connect(it, it1) } }
    }

    private fun add_acls(mRID: String, phs: PhaseCode): AcLineSegment
    {
        val acls = AcLineSegment(mRID=mRID)
        val t1 = Terminal().apply {
            phases = phs
            conductingEquipment = acls}
        acls.addTerminal(t1)
        val t2 = Terminal().apply {
            phases = phs
            conductingEquipment = acls }
        acls.addTerminal(t2)
        service.add(acls)
        service.add(t1)
        service.add(t2)
        return acls
    }

    @Test
    internal fun acls1() {
        val net =  TestNetwork1()
        assertThat(net.service.get(AcLineSegment::class, mRID ="acls1")?.mRID, equalTo("acls1"))
    }

    @Test
    internal fun acls1_phases() {
        val net = TestNetwork1()
        assertThat(net.service.get(AcLineSegment::class, mRID ="acls1")?.getTerminal(1)?.phases, equalTo(PhaseCode.ABC))
    }
    @Test
    internal fun basic_tracing() {
        val net = TestNetwork1()
        val acls1 = net.service.get(ConductingEquipment::class, mRID = "acls1")!!
        val expected = net.service.setOf<ConductingEquipment>()
        val visited: MutableSet<ConductingEquipment> = HashSet()
        Tracing.connectedEquipmentTrace().addStepAction{ce, _ -> visited.add(ce)}.run(acls1)
        assertThat(visited, equalTo(expected))

    }
}

internal class PhaseBalancingTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()



}
    
