/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


@file:Suppress("PropertyName")

package com.zepben.evolve.services.network.tracing.phases

import com.zepben.evolve.cim.iec61968.assetinfo.OverheadWireInfo
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.cim.iec61970.base.wires.SinglePhaseKind.*
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.testdata.PhasesTestNetwork
import com.zepben.evolve.services.network.testdata.TestDataCreators.createSourceForConnecting
import com.zepben.evolve.services.network.testdata.TestDataCreators.createTerminal
import com.zepben.evolve.services.network.testdata.TestNetworks
import com.zepben.evolve.services.network.tracing.Tracing
import com.zepben.evolve.services.network.tracing.phases.PhaseDirection.*
import com.zepben.evolve.services.network.tracing.phases.PhaseDirection.NONE
import com.zepben.testutils.exception.ExpectException.expect
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.function.Function

class SetPhasesTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    @Suppress("PrivatePropertyName")
    private val P_NONE = SinglePhaseKind.NONE

    @Test
    internal fun setPhasesTest() {
        val n = TestNetworks.getNetwork(1)

        val sw = n.get<Breaker>("node6")!!
        assertThat(sw, notNullValue())
        assertThat(sw.isOpen(X), equalTo(true))
        assertThat(sw.isOpen(Y), equalTo(true))
        doSetPhasesTrace(n)

        // Check various points to make sure phases have been applied during the trace.
        checkExpectedPhases(getT(n, "acLineSegment0", 1), arrayOf(A, B, C, N), arrayOf(IN, IN, IN, IN))
        checkExpectedPhases(getT(n, "acLineSegment0", 2), arrayOf(A, B, C, N), arrayOf(OUT, OUT, OUT, OUT))
        checkExpectedPhases(getT(n, "acLineSegment1", 1), arrayOf(A, B, C, N), arrayOf(IN, IN, IN, IN))
        checkExpectedPhases(getT(n, "acLineSegment4", 1), arrayOf(A, B), arrayOf(IN, IN))
        checkExpectedPhases(getT(n, "node4", 1), arrayOf(A, B), arrayOf(IN, IN))
        checkExpectedPhases(getT(n, "node4", 2), arrayOf(A, B), arrayOf(OUT, OUT))
        checkExpectedPhases(getT(n, "node4", 3), arrayOf(A, B), arrayOf(OUT, OUT))
        checkExpectedPhases(getT(n, "node8", 1), arrayOf(A), arrayOf(IN))
        checkExpectedPhases(getT(n, "node5", 1), arrayOf(A, B), arrayOf(IN, IN))
        checkExpectedPhases(getT(n, "node5", 2), arrayOf(A, B), arrayOf(OUT, OUT))
        checkExpectedPhases(getT(n, "node5", 3), arrayOf(A, B), arrayOf(OUT, OUT))
        checkExpectedPhases(getT(n, "node9", 1), arrayOf(B), arrayOf(IN))
        checkExpectedPhases(getT(n, "node6", 1), arrayOf(A, B), arrayOf(IN, IN))
        checkExpectedPhases(getT(n, "node6", 2), arrayOf(B, C), arrayOf(IN, IN))
        checkExpectedPhases(getT(n, "acLineSegment2", 2), arrayOf(A, B, C, N), arrayOf(OUT, OUT, OUT, OUT))
        checkExpectedPhases(getT(n, "acLineSegment3", 1), arrayOf(A, B, C, N), arrayOf(IN, IN, IN, IN))
        checkExpectedPhases(getT(n, "acLineSegment9", 2), arrayOf(B, C), arrayOf(IN, IN))
        checkExpectedPhases(getT(n, "node2", 1), arrayOf(A, B, C, N), arrayOf(IN, IN, IN, IN))
    }

    @Test
    internal fun setPhasesMultiSourceTest() {
        val n = TestNetworks.getNetwork(5)

        val sw = n.get<Breaker>("node1")!!
        assertThat(sw, notNullValue())
        assertThat(sw.isOpen(A), equalTo(true))
        assertThat(sw.isOpen(B), equalTo(false))
        assertThat(sw.isOpen(C), equalTo(true))
        assertThat(sw.isOpen(N), equalTo(false))

        doSetPhasesTrace(n)

        // Check various points to make sure phases have been applied during the trace.
        checkExpectedPhases(getT(n, "node0", 1), arrayOf(A, B, C, N), arrayOf(OUT, BOTH, OUT, BOTH))
        checkExpectedPhases(getT(n, "acLineSegment0", 1), arrayOf(A, B, C, N), arrayOf(IN, BOTH, IN, BOTH))
        checkExpectedPhases(getT(n, "acLineSegment0", 2), arrayOf(A, B, C, N), arrayOf(OUT, BOTH, OUT, BOTH))
        checkExpectedPhases(getT(n, "node1", 1), arrayOf(A, B, C, N), arrayOf(IN, BOTH, IN, BOTH))
        checkExpectedPhases(getT(n, "node1", 2), arrayOf(A, B, C, N), arrayOf(IN, BOTH, IN, BOTH))
        checkExpectedPhases(getT(n, "acLineSegment1", 1), arrayOf(A, B, C, N), arrayOf(OUT, BOTH, OUT, BOTH))
        checkExpectedPhases(getT(n, "acLineSegment1", 2), arrayOf(A, B, C, N), arrayOf(IN, BOTH, IN, BOTH))
        checkExpectedPhases(getT(n, "node2", 1), arrayOf(A, B, C, N), arrayOf(OUT, BOTH, OUT, BOTH))
    }

    @Test
    internal fun setPhasesTestCrossPhases() {
        val n = TestNetworks.getNetwork(1)

        val sw = n.get<Breaker>("node6")!!
        assertThat(sw, notNullValue())
        assertThat(sw.isOpen(X), equalTo(true))
        sw.setOpen(false, Y)

        expect { doSetPhasesTrace(n) }
            .toThrow(IllegalStateException::class.java)
    }

    // Build a small network with a closed loop and test tracing phases both ways
    @Test
    internal fun setPhasesInClosedLoopTest() {
        val n = NetworkService()

        val n0 = createSourceForConnecting(n, "n0", 1, PhaseCode.A)
        for (i in 0..4) {
            val c = AcLineSegment("c$i").apply { name = "c"; addTerminals(2, PhaseCode.A) }
            n.add(c)
        }

        for (i in 1..3) {
            val bn = Junction("n$i").apply { addTerminals(2, PhaseCode.A) }
            n.add(bn)
        }

        val n4 = Junction("n4").apply { addTerminal(createTerminal(n, this, PhaseCode.A, 1)) }
        n.add(n4)

        assertThat(n.connect(getT(n, "n0", 1), getT(n, "c0", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c0", 2), getT(n, "n1", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "n1", 2), getT(n, "c1", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c1", 2), getT(n, "n2", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "n2", 2), getT(n, "c2", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c2", 2), getT(n, "n3", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "n3", 2), getT(n, "c3", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c3", 2), getT(n, "n1", 2)), equalTo(true))
        assertThat(n.connect(getT(n, "c4", 1), getT(n, "n3", 2)), equalTo(true))
        assertThat(n.connect(getT(n, "n4", 1), getT(n, "c4", 2)), equalTo(true))

        n0.terminals.forEach { t ->
            for (phase in t.phases.singlePhases()) {
                t.normalPhases(phase).add(A, OUT)
                t.currentPhases(phase).add(A, OUT)
            }
            checkExpectedPhases(t, arrayOf(A), arrayOf(OUT))
        }

        doSetPhasesTrace(n0)
        checkExpectedPhases(getT(n, "n0", 1), arrayOf(A), arrayOf(OUT))
        checkExpectedPhases(getT(n, "c0", 1), arrayOf(A), arrayOf(IN))
        checkExpectedPhases(getT(n, "c0", 2), arrayOf(A), arrayOf(OUT))
        checkExpectedPhases(getT(n, "n1", 1), arrayOf(A), arrayOf(IN))
        checkExpectedPhases(getT(n, "n1", 2), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "c1", 1), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "c1", 2), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "n2", 1), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "n2", 2), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "c2", 1), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "c2", 2), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "n3", 1), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "n3", 2), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "c3", 1), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "c3", 2), arrayOf(A), arrayOf(BOTH))
        checkExpectedPhases(getT(n, "c4", 1), arrayOf(A), arrayOf(IN))
        checkExpectedPhases(getT(n, "c4", 2), arrayOf(A), arrayOf(OUT))
        checkExpectedPhases(getT(n, "n4", 1), arrayOf(A), arrayOf(IN))
    }

    @Test
    internal fun setPhasesThroughFeederCbs() {
        val n = NetworkService()

        createSourceForConnecting(n, "n0", 1, PhaseCode.ABCN)
        createSourceForConnecting(n, "n1", 1, PhaseCode.ABCN)

        fun createBreaker(id: String, open: Boolean, normallyOpen: Array<Boolean>): Breaker {
            return Breaker(id).apply {
                name = id
                addTerminals(2, PhaseCode.ABCN)
                setOpen(isOpen = open)
                for (i in normallyOpen.indices)
                    setNormallyOpen(normallyOpen[i], PhaseCode.ABCN.singlePhases()[i])
                addContainer(Substation())
                assertThat(this.isSubstationBreaker, equalTo(true))
            }
        }

        n.add(createBreaker("f0", true, arrayOf(false, true, false, true)))
        n.add(createBreaker("f1", true, arrayOf(false, false, false, false)))
        n.add(createBreaker("f2", true, arrayOf(false, false, false, false)))
        n.add(createBreaker("f3", true, arrayOf(true, false, false, true)))
        n.add(createBreaker("f4", false, arrayOf(false, false, false, false)))

        val overheadWireInfo = OverheadWireInfo()
        for (i in 0..5) {
            n.add(AcLineSegment("c$i").apply {
                name = "c$i"
                assetInfo = overheadWireInfo
                addTerminals(2, PhaseCode.ABCN)
            })
        }

        assertThat(n.connect(getT(n, "n0", 1), getT(n, "c0", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c0", 2), getT(n, "f0", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "f0", 2), getT(n, "c1", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c1", 2), getT(n, "f1", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "f1", 2), getT(n, "c2", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c2", 2), getT(n, "f2", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "f2", 2), getT(n, "c3", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c3", 2), getT(n, "f3", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "f3", 2), getT(n, "c4", 1)), equalTo(true))
        assertThat(n.connect(getT(n, "c4", 2), getT(n, "n1", 1)), equalTo(true))

        doSetPhasesTrace(n)

        checkExpectedPhases(getT(n, "n0", 1), arrayOf(A, B, C, N), arrayOf(OUT, OUT, OUT, OUT))
        checkExpectedPhases(getT(n, "c0", 1), arrayOf(A, B, C, N), arrayOf(IN, IN, IN, IN))
        checkExpectedPhases(getT(n, "c0", 2), arrayOf(A, B, C, N), arrayOf(OUT, OUT, OUT, OUT))
        checkExpectedPhases(getT(n, "f0", 1), arrayOf(A, B, C, N), arrayOf(IN, IN, IN, IN))
        checkExpectedPhases(
            getT(n, "f0", 2),
            arrayOf(A, B, C, P_NONE),
            arrayOf(OUT, IN, OUT, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "c1", 1),
            arrayOf(A, B, C, P_NONE),
            arrayOf(IN, OUT, IN, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "c1", 2),
            arrayOf(A, B, C, P_NONE),
            arrayOf(OUT, IN, OUT, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "f1", 1),
            arrayOf(A, B, C, P_NONE),
            arrayOf(IN, OUT, IN, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "f1", 2),
            arrayOf(A, B, C, P_NONE),
            arrayOf(OUT, IN, BOTH, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "c2", 1),
            arrayOf(A, B, C, P_NONE),
            arrayOf(IN, OUT, BOTH, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "c2", 2),
            arrayOf(A, B, C, P_NONE),
            arrayOf(OUT, IN, BOTH, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "f2", 1),
            arrayOf(A, B, C, P_NONE),
            arrayOf(IN, OUT, BOTH, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "f2", 2),
            arrayOf(A, B, C, P_NONE),
            arrayOf(OUT, IN, IN, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "c3", 1),
            arrayOf(A, B, C, P_NONE),
            arrayOf(IN, OUT, OUT, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "c3", 2),
            arrayOf(A, B, C, P_NONE),
            arrayOf(OUT, IN, IN, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(
            getT(n, "f3", 1),
            arrayOf(A, B, C, P_NONE),
            arrayOf(IN, OUT, OUT, NONE),
            arrayOf(P_NONE, P_NONE, P_NONE, P_NONE),
            arrayOf(NONE, NONE, NONE, NONE)
        )
        checkExpectedPhases(getT(n, "f3", 2), arrayOf(A, B, C, N), arrayOf(IN, IN, IN, IN))
        checkExpectedPhases(getT(n, "c4", 1), arrayOf(A, B, C, N), arrayOf(OUT, OUT, OUT, OUT))
        checkExpectedPhases(getT(n, "c4", 2), arrayOf(A, B, C, N), arrayOf(IN, IN, IN, IN))
        checkExpectedPhases(getT(n, "n1", 1), arrayOf(A, B, C, N), arrayOf(OUT, OUT, OUT, OUT))
        checkExpectedPhases(getT(n, "f4", 1), arrayOf(P_NONE, P_NONE, P_NONE, P_NONE), arrayOf(NONE, NONE, NONE, NONE))
        checkExpectedPhases(getT(n, "f4", 2), arrayOf(P_NONE, P_NONE, P_NONE, P_NONE), arrayOf(NONE, NONE, NONE, NONE))
    }

    @Test
    internal fun canStartFromSingleTerminalFeederCbWithAnySequence() {
        val n = NetworkService()

        val substation = Substation()
        fun createBreaker(id: String, sequenceNumber: Int): Breaker {
            return Breaker(id).apply {
                name = id
                addTerminal(Terminal().also { it.conductingEquipment = this; it.sequenceNumber = sequenceNumber })
                addContainer(substation)
                substation.addEquipment(this)
            }
        }

        val fcb1: Breaker = createBreaker("f1", 1)
        val fcb2: Breaker = createBreaker("f2", 2)
        val energySource1 = createSourceForConnecting(n, "energySource1", 1, PhaseCode.ABC)
        val energySource2 = createSourceForConnecting(n, "energySource2", 1, PhaseCode.ABC)

        n.connect(fcb1.getTerminal(1)!!, energySource1.getTerminal(1)!!)
        n.connect(fcb2.getTerminal(2)!!, energySource1.getTerminal(1)!!)

        n.add(fcb1)
        n.add(fcb2)
        n.add(energySource1)
        n.add(energySource2)

        doSetPhasesTrace(n)
    }

    //
    // nominal
    // ABC -> XY -> XY -> BC
    // traced
    // ABC -> BC -> BC -> BC
    //
    @Test
    internal fun tracesXyDownstreamForNominalPhases() {
        val network = PhasesTestNetwork
            .from(PhaseCode.ABC)
            .to(PhaseCode.XY)
            .to(PhaseCode.XY)
            .to(PhaseCode.BC)
            .build()

        PhaseValidator.validatePhases(network, "c1", B, C)
        PhaseValidator.validatePhases(network, "c2", B, C)
        PhaseValidator.validatePhases(network, "c3", B, C)
    }

    //
    // nominal
    // ABC -> XY -> X -> B
    //           -> Y -> C
    // traced
    // ABC -> BC -> B -> B
    //           -> C -> C
    //
    @Test
    internal fun testABCtoXYtoSplitXYtoBC() {
        val network = PhasesTestNetwork
            .from(PhaseCode.ABC)
            .to(PhaseCode.XY)
            .to(PhaseCode.X)
            .to(PhaseCode.B)
            .splitFromTo("c1", PhaseCode.Y)
            .to(PhaseCode.C)
            .build()

        PhaseValidator.validatePhases(network, "c1", B, C)
        PhaseValidator.validatePhases(network, "c2", B)
        PhaseValidator.validatePhases(network, "c3", B)
        PhaseValidator.validatePhases(network, "c4", C)
        PhaseValidator.validatePhases(network, "c5", C)
    }

    //
    // nominal
    // ABC -> XY -> B
    //           -> C
    // traced
    // ABC -> BC -> B
    //           -> C
    //
    @Test
    internal fun testABCtoXYtoSplitBC() {
        val network = PhasesTestNetwork
            .from(PhaseCode.ABC)
            .to(PhaseCode.XY)
            .to(PhaseCode.B)
            .splitFromTo("c1", PhaseCode.C)
            .build()

        PhaseValidator.validatePhases(network, "c1", B, C)
        PhaseValidator.validatePhases(network, "c2", B)
        PhaseValidator.validatePhases(network, "c3", C)
    }

    //
    // nominal
    // ABC -> XY -> AC
    //           -> BC
    // traced
    // ABC -> AB -> AC
    //           -> BC
    //
    @Test
    @Disabled(value = "Use case not supported")
    internal fun testABCtoXYtoConflictingACBC() {
        val network = PhasesTestNetwork
            .from(PhaseCode.ABC)
            .to(PhaseCode.XY)
            .to(PhaseCode.AC)
            .splitFromTo("c1", PhaseCode.BC)
            .build()

        PhaseValidator.validatePhases(network, "c1", A, B)
        PhaseValidator.validatePhases(network, "c2", A, C)
        PhaseValidator.validatePhases(network, "c3", B, C)
    }

    @Test
    internal fun processesXYLoop() {
        val builder = PhasesTestNetwork
            .from(PhaseCode.ABC)
            .to(PhaseCode.XY)
            .to(PhaseCode.XY)
            .to(PhaseCode.XY)

        // Create a loop
        builder.network.connect(
            builder.network.get<ConductingEquipment>("c1")!!.getTerminal(2)!!,
            builder.network.get<ConductingEquipment>("c3")!!.getTerminal(2)!!
        )

        val network = builder.build()

        PhaseValidator.validatePhases(network, "c1", A, B)
        PhaseValidator.validatePhases(network, "c2", A, B)
        PhaseValidator.validatePhases(network, "c3", A, B)
    }

    @Test
    internal fun processesLongXYChain() {
        val builder = PhasesTestNetwork
            .from(PhaseCode.ABC)

        for (i in 1..3000)
            builder.to(PhaseCode.XY)

        assertThat(builder.build(), notNullValue())
    }

    private fun doSetPhasesTrace(n: NetworkService) {
        Tracing.setPhases().run(n)
        n.sequenceOf<EnergySource>().forEach { PhaseLogger.trace(it) }
    }

    private fun doSetPhasesTrace(start: ConductingEquipment) {
        assertThat(start.numTerminals(), equalTo(1))

        Tracing.setPhases().run(start.getTerminal(1)!!, emptyList())
        PhaseLogger.trace(start)
    }
    // Get a terminal from an asset in the network
    private fun getT(n: NetworkService, id: String, terminalId: Int) =
        n.get<ConductingEquipment>(id)!!.getTerminal(terminalId)!!

    private fun checkExpectedPhases(
        t: Terminal,
        phases: Array<SinglePhaseKind>,
        directions: Array<PhaseDirection>
    ) {
        checkExpectedPhases(t, phases, directions, phases, directions)
    }

    private fun checkExpectedPhases(
        t: Terminal,
        normalPhases: Array<SinglePhaseKind>,
        normalDirections: Array<PhaseDirection>,
        currentPhases: Array<SinglePhaseKind>,
        currentDirections: Array<PhaseDirection>
    ) {
        checkExpectedPhases(t, normalPhases, normalDirections) { phase -> t.normalPhases(phase) }
        checkExpectedPhases(t, currentPhases, currentDirections) { phase -> t.currentPhases(phase) }
    }

    private fun checkExpectedPhases(
        t: Terminal?,
        singlePhaseKinds: Array<SinglePhaseKind>,
        directions: Array<PhaseDirection>,
        phaseStatusSelector: Function<SinglePhaseKind, PhaseStatus>
    ) {
        assertThat(t, notNullValue())
        assertThat(directions.size, equalTo(singlePhaseKinds.size))
        assertThat(t!!.phases.singlePhases().size, equalTo(singlePhaseKinds.size))
        for (i in singlePhaseKinds.indices) {
            val ps = phaseStatusSelector.apply(t.phases.singlePhases()[i])
            assertThat(ps.phase(), equalTo(singlePhaseKinds[i]))
            assertThat(ps.direction(), equalTo(directions[i]))
        }
    }

    private fun ConductingEquipment.addTerminals(count: Int, phases: PhaseCode = PhaseCode.ABC) {
        for (i in 1..count)
            addTerminal(Terminal().also { it.conductingEquipment = this; it.phases = phases })
    }

    @Test
    // Set Phases from an known phases of a Feeder NormalHeadTerminal
    internal fun setPhasesfromKnownHeadTerminal() {
        val network = NetworkService()
        val terminal = Terminal("t1").apply { phases = PhaseCode.AB}
        val feeder = Feeder().apply { normalHeadTerminal = terminal}
        network.add(terminal)
        network.add(feeder)
        Tracing.setPhases().run(network)
        checkExpectedPhases(terminal, arrayOf(A, B), arrayOf(OUT, OUT))
    }
}

