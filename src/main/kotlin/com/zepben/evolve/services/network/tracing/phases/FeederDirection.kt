/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network.tracing.phases

/***
 * Enumeration of directions along a feeder at a terminal.
 *
 * @property NONE The terminal is not on a feeder.
 * @property UPSTREAM The terminal can be used to trace upstream towards the feeder head.
 * @property DOWNSTREAM The terminal can be used to trace downstream away from the feeder head.
 * @property BOTH The terminal is part of a loop on the feeder and tracing in either direction will allow you
 *                to trace upstream towards the feeder head, or downstream away from the feeder head.
 */
enum class FeederDirection(private val value: Int) {

    NONE(0),
    UPSTREAM(1),
    DOWNSTREAM(2),
    BOTH(3);

    companion object {
        private val directionsByValues: Array<FeederDirection> = enumValues<FeederDirection>().sortedBy { it.value }.toTypedArray()

        @JvmStatic
        fun from(value: Int): FeederDirection {
            return if (value <= 0 || value > 3) NONE else directionsByValues[value]
        }
    }

    fun value(): Int {
        return value
    }

    fun has(other: FeederDirection): Boolean {
        return if (this == BOTH) other != NONE else this == other
    }

    operator fun plus(rhs: FeederDirection): FeederDirection {
        return directionsByValues[value or rhs.value]
    }

    operator fun minus(rhs: FeederDirection): FeederDirection {
        return directionsByValues[value - (value and rhs.value)]
    }
}
