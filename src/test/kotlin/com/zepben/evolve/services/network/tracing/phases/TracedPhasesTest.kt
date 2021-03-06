/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network.tracing.phases

import com.zepben.evolve.cim.iec61970.base.wires.SinglePhaseKind
import com.zepben.testutils.exception.ExpectException.expect
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class TracedPhasesTest {

    @Test
    fun testGetPhase() {
        val tracedPhases = newTestPhaseObject()

        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.A), equalTo(SinglePhaseKind.A))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.B), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.C), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.N), equalTo(SinglePhaseKind.N))

        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.A), equalTo(SinglePhaseKind.N))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.B), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.C), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.N), equalTo(SinglePhaseKind.A))
    }

    @Test
    fun testGetDirection() {
        val tracedPhases = newTestPhaseObject()

        assertThat(tracedPhases.directionNormal(SinglePhaseKind.A), equalTo(PhaseDirection.IN))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.B), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.C), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.N), equalTo(PhaseDirection.BOTH))

        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.A), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.B), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.C), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.N), equalTo(PhaseDirection.IN))
    }

    @Test
    fun testSet() {
        val tracedPhases = newTestPhaseObject()

        /* -- Setting -- */
        assertThat(tracedPhases.setNormal(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.A), equalTo(true))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.C), equalTo(true))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.N), equalTo(true))

        assertThat(tracedPhases.setCurrent(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.A), equalTo(true))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.C), equalTo(true))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.N), equalTo(true))

        // Returns false if no changes were done.
        assertThat(tracedPhases.setNormal(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.A), equalTo(false))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.B), equalTo(false))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.C), equalTo(false))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.N), equalTo(false))

        assertThat(tracedPhases.setCurrent(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.A), equalTo(false))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.B), equalTo(false))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.C), equalTo(false))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.N), equalTo(false))

        /* -- Getting Phase-- */
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.A), equalTo(SinglePhaseKind.N))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.B), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.C), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.N), equalTo(SinglePhaseKind.A))

        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.A), equalTo(SinglePhaseKind.A))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.B), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.C), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.N), equalTo(SinglePhaseKind.N))

        /* -- Getting Direction-- */
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.A), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.B), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.C), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.N), equalTo(PhaseDirection.IN))

        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.A), equalTo(PhaseDirection.IN))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.B), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.C), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.N), equalTo(PhaseDirection.BOTH))

        /* -- Setting -- */
        assertThat(tracedPhases.setNormal(SinglePhaseKind.N, PhaseDirection.IN, SinglePhaseKind.A), equalTo(true))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.C, PhaseDirection.OUT, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.B, PhaseDirection.BOTH, SinglePhaseKind.C), equalTo(true))
        assertThat(tracedPhases.setNormal(SinglePhaseKind.A, PhaseDirection.BOTH, SinglePhaseKind.N), equalTo(true))

        assertThat(tracedPhases.setCurrent(SinglePhaseKind.A, PhaseDirection.BOTH, SinglePhaseKind.A), equalTo(true))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.B, PhaseDirection.BOTH, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.C, PhaseDirection.OUT, SinglePhaseKind.C), equalTo(true))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.N, PhaseDirection.IN, SinglePhaseKind.N), equalTo(true))

        /* -- Getting Phase-- */
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.A), equalTo(SinglePhaseKind.N))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.B), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.C), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.N), equalTo(SinglePhaseKind.A))

        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.A), equalTo(SinglePhaseKind.A))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.B), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.C), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.N), equalTo(SinglePhaseKind.N))

        /* -- Getting Direction-- */
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.A), equalTo(PhaseDirection.IN))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.B), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.C), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.N), equalTo(PhaseDirection.BOTH))

        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.A), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.B), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.C), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.N), equalTo(PhaseDirection.IN))

        // Setting NONE to the direction clears the whole phase
        tracedPhases.setNormal(SinglePhaseKind.N, PhaseDirection.NONE, SinglePhaseKind.A)
        tracedPhases.setCurrent(SinglePhaseKind.A, PhaseDirection.NONE, SinglePhaseKind.A)

        assertThat(tracedPhases.directionNormal(SinglePhaseKind.A), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.A), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.A), equalTo(SinglePhaseKind.NONE))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.A), equalTo(SinglePhaseKind.NONE))

        // Setting NONE to the phase clears the whole phase
        assertThat(tracedPhases.setNormal(SinglePhaseKind.NONE, PhaseDirection.NONE, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.setCurrent(SinglePhaseKind.NONE, PhaseDirection.NONE, SinglePhaseKind.B), equalTo(true))

        assertThat(tracedPhases.directionNormal(SinglePhaseKind.B), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.B), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.B), equalTo(SinglePhaseKind.NONE))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.B), equalTo(SinglePhaseKind.NONE))
    }

    @Test
    fun testAdd() {
        val tracedPhases = TracedPhases()

        /* -- Adding -- */
        assertThat(tracedPhases.addNormal(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.A), equalTo(true))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.C), equalTo(true))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.N), equalTo(true))

        assertThat(tracedPhases.addCurrent(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.A), equalTo(true))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.C), equalTo(true))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.N), equalTo(true))

        // Returns false if no changes were done.
        assertThat(tracedPhases.addNormal(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.A), equalTo(false))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.B), equalTo(false))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.C), equalTo(false))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.N), equalTo(false))

        assertThat(tracedPhases.addCurrent(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.A), equalTo(false))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.B), equalTo(false))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.C), equalTo(false))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.N), equalTo(false))

        /* -- Getting Phase-- */
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.A), equalTo(SinglePhaseKind.N))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.B), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.C), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.N), equalTo(SinglePhaseKind.A))

        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.A), equalTo(SinglePhaseKind.A))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.B), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.C), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.N), equalTo(SinglePhaseKind.N))

        /* -- Getting Direction-- */
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.A), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.B), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.C), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.N), equalTo(PhaseDirection.IN))

        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.A), equalTo(PhaseDirection.IN))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.B), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.C), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.N), equalTo(PhaseDirection.BOTH))

        /* -- Adding -- */
        assertThat(tracedPhases.addNormal(SinglePhaseKind.N, PhaseDirection.NONE, SinglePhaseKind.A), equalTo(false))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.C, PhaseDirection.IN, SinglePhaseKind.B), equalTo(false))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.B, PhaseDirection.IN, SinglePhaseKind.C), equalTo(true))
        assertThat(tracedPhases.addNormal(SinglePhaseKind.A, PhaseDirection.OUT, SinglePhaseKind.N), equalTo(true))

        assertThat(tracedPhases.addCurrent(SinglePhaseKind.A, PhaseDirection.OUT, SinglePhaseKind.A), equalTo(true))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.B, PhaseDirection.IN, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.C, PhaseDirection.NONE, SinglePhaseKind.C), equalTo(false))
        assertThat(tracedPhases.addCurrent(SinglePhaseKind.N, PhaseDirection.OUT, SinglePhaseKind.N), equalTo(false))

        /* -- Getting Phase-- */
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.A), equalTo(SinglePhaseKind.N))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.B), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.C), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseNormal(SinglePhaseKind.N), equalTo(SinglePhaseKind.A))

        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.A), equalTo(SinglePhaseKind.A))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.B), equalTo(SinglePhaseKind.B))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.C), equalTo(SinglePhaseKind.C))
        assertThat(tracedPhases.phaseCurrent(SinglePhaseKind.N), equalTo(SinglePhaseKind.N))

        /* -- Getting Direction-- */
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.A), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.B), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.C), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.N), equalTo(PhaseDirection.BOTH))

        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.A), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.B), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.C), equalTo(PhaseDirection.BOTH))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.N), equalTo(PhaseDirection.BOTH))

        expect { tracedPhases.addNormal(SinglePhaseKind.A, PhaseDirection.BOTH, SinglePhaseKind.A) }
            .toThrow(UnsupportedOperationException::class.java)
    }

    @Test
    fun testRemoveDirection() {
        val tracedPhases = newTestPhaseObject()

        tracedPhases.addNormal(SinglePhaseKind.A, PhaseDirection.OUT, SinglePhaseKind.A)
        tracedPhases.addNormal(SinglePhaseKind.B, PhaseDirection.IN, SinglePhaseKind.B)

        tracedPhases.addCurrent(SinglePhaseKind.B, PhaseDirection.IN, SinglePhaseKind.C)
        tracedPhases.addCurrent(SinglePhaseKind.A, PhaseDirection.OUT, SinglePhaseKind.N)

        tracedPhases.removeNormal(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.A)
        tracedPhases.removeNormal(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.B)
        tracedPhases.removeNormal(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.C)
        tracedPhases.removeNormal(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.N)

        tracedPhases.removeCurrent(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.A)
        tracedPhases.removeCurrent(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.B)
        tracedPhases.removeCurrent(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.C)
        tracedPhases.removeCurrent(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.N)

        assertThat(tracedPhases.directionNormal(SinglePhaseKind.A), equalTo(PhaseDirection.OUT))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.B), equalTo(PhaseDirection.IN))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.C), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.N), equalTo(PhaseDirection.NONE))

        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.A), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.B), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.C), equalTo(PhaseDirection.IN))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.N), equalTo(PhaseDirection.OUT))

        assertThat(tracedPhases.removeNormal(SinglePhaseKind.A, SinglePhaseKind.A), equalTo(true))
        assertThat(tracedPhases.removeNormal(SinglePhaseKind.B, SinglePhaseKind.B), equalTo(true))
        assertThat(tracedPhases.removeNormal(SinglePhaseKind.C, SinglePhaseKind.C), equalTo(false))
        assertThat(tracedPhases.removeNormal(SinglePhaseKind.N, SinglePhaseKind.N), equalTo(false))

        assertThat(tracedPhases.removeCurrent(SinglePhaseKind.N, SinglePhaseKind.A), equalTo(false))
        assertThat(tracedPhases.removeCurrent(SinglePhaseKind.C, SinglePhaseKind.B), equalTo(false))
        assertThat(tracedPhases.removeCurrent(SinglePhaseKind.B, SinglePhaseKind.C), equalTo(true))
        assertThat(tracedPhases.removeCurrent(SinglePhaseKind.A, SinglePhaseKind.N), equalTo(true))

        assertThat(tracedPhases.directionNormal(SinglePhaseKind.A), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.B), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.C), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionNormal(SinglePhaseKind.N), equalTo(PhaseDirection.NONE))

        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.A), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.B), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.C), equalTo(PhaseDirection.NONE))
        assertThat(tracedPhases.directionCurrent(SinglePhaseKind.N), equalTo(PhaseDirection.NONE))
    }

    @Test
    fun testInvalidCoreNormal() {
        expect { newTestPhaseObject().phaseNormal(SinglePhaseKind.INVALID) }
            .toThrow(IllegalArgumentException::class.java)
    }

    @Test
    fun testCrossingPhasesExceptionNormal() {
        expect { newTestPhaseObject().addNormal(SinglePhaseKind.B, PhaseDirection.BOTH, SinglePhaseKind.A) }
            .toThrow(UnsupportedOperationException::class.java)
    }

    @Test
    fun testInvalidCoreCurrent() {
        expect { newTestPhaseObject().phaseCurrent(SinglePhaseKind.INVALID) }
            .toThrow(IllegalArgumentException::class.java)
    }

    @Test
    fun testCrossingPhasesExceptionCurrent() {
        expect { newTestPhaseObject().addCurrent(SinglePhaseKind.B, PhaseDirection.BOTH, SinglePhaseKind.A) }
            .toThrow(UnsupportedOperationException::class.java)
    }

    @Test
    fun removingSomethingNotPresentNormal() {
        val tracedPhases = newTestPhaseObject()
        assertThat(tracedPhases.removeNormal(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.A), equalTo(false))
    }

    @Test
    fun removingSomethingNotPresentCurrent() {
        val tracedPhases = newTestPhaseObject()
        assertThat(tracedPhases.removeCurrent(SinglePhaseKind.A, PhaseDirection.OUT, SinglePhaseKind.N), equalTo(false))
    }

    private fun newTestPhaseObject() = TracedPhases().apply {
        setNormal(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.A)
        setNormal(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.B)
        setNormal(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.C)
        setNormal(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.N)

        setCurrent(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.A)
        setCurrent(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.B)
        setCurrent(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.C)
        setCurrent(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.N)
    }

}
