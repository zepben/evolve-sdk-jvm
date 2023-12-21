/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.networktrace.conditions

import com.zepben.evolve.cim.iec61970.base.core.PhaseCode
import com.zepben.evolve.services.network.tracing.connectivity.ConnectivityResult
import com.zepben.evolve.services.network.tracing.connectivity.TerminalConnectivityConnected
import com.zepben.evolve.services.network.tracing.networktrace.NetworkTraceStep
import com.zepben.evolve.services.network.tracing.traversalV2.QueueConditionWithContextData
import com.zepben.evolve.services.network.tracing.traversalV2.StepContext
import java.util.IdentityHashMap

internal class PhaseCondition<T>(
    val phases: PhaseCode
) : QueueConditionWithContextData<NetworkTraceStep<T>, ConnectivityResult> {

    private val terminalConnectivity = TerminalConnectivityConnected()
    private val precalculatedResults = IdentityHashMap<NetworkTraceStep<T>, ConnectivityResult>()

    override fun shouldQueue(nextItem: NetworkTraceStep<T>, currentContext: StepContext): Boolean {
        val connectivity = currentContext.terminalConnectivity()
        TODO("Fix !! in the code below")
        return terminalConnectivity.terminalConnectivity(nextItem.fromTerminal!!, nextItem.toTerminal!!, connectivity.toNominalPhases.toSet())
            .also { precalculatedResults[nextItem] = it }
            .nominalPhasePaths.isNotEmpty()
    }

    override fun computeInitialValue(nextItem: NetworkTraceStep<T>): ConnectivityResult {
        TODO("Fix !! in the code below")
        return terminalConnectivity.terminalConnectivity(nextItem.toTerminal!!, nextItem.toTerminal!!, phases.singlePhases.toSet())
    }

    override fun computeNextValue(nextItem: NetworkTraceStep<T>, value: ConnectivityResult): ConnectivityResult {
        return precalculatedResults.remove(nextItem) ?: error("INTERNAL ERROR: value should have been stored as part of queuing")
    }

    override val key: String
        get() = contextKey

    companion object {
        internal const val contextKey = "sdk:phase"
    }
}

fun StepContext.terminalConnectivity(): ConnectivityResult {
    return this.getData(PhaseCondition.contextKey) ?: error("Your trace needs to have PhaseCondition added to access this")
}
