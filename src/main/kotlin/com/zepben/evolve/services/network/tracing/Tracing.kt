/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network.tracing

import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.Terminal
import com.zepben.evolve.cim.iec61970.base.wires.SinglePhaseKind
import com.zepben.evolve.services.network.tracing.feeder.AssignToFeeders
import com.zepben.evolve.services.network.tracing.feeder.AssociatedTerminalTrace
import com.zepben.evolve.services.network.tracing.phases.*
import com.zepben.evolve.services.network.tracing.traversals.BasicQueue
import com.zepben.evolve.services.network.tracing.traversals.BasicTracker
import com.zepben.evolve.services.network.tracing.traversals.BasicTraversal
import com.zepben.evolve.services.network.tracing.tree.DownstreamTree

/**
 * A utility class intended to be a one stop shop to instantiate all your common network traces!
 */
object Tracing {

    @JvmStatic
    fun <T> createBasicDepthTrace(queueNext: BasicTraversal.QueueNext<T>): BasicTraversal<T> {
        return BasicTraversal(queueNext, BasicQueue.depthFirst(), BasicTracker())
    }

    @JvmStatic
    fun <T> createBasicBreadthTrace(queueNext: BasicTraversal.QueueNext<T>): BasicTraversal<T> {
        return BasicTraversal(queueNext, BasicQueue.breadthFirst(), BasicTracker())
    }

    /**
     * Creates a new traversal that traces equipment that are connected. This ignores phases, open status etc.
     * It is purely to trace equipment that are connected in any way.
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun connectedEquipmentTrace(): BasicTraversal<ConductingEquipment> = ConnectedEquipmentTrace.newConnectedEquipmentTrace()

    /**
     * Creates a new traversal that traces equipment that are connected. This ignores phases, open status etc.
     * It is purely to trace equipment that are connected in any way.
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun connectedEquipmentBreadthTrace(): BasicTraversal<ConductingEquipment> = ConnectedEquipmentTrace.newConnectedEquipmentBreadthTrace()

    /**
     * Creates a new traversal that traces equipment that are connected stopping at normally open points. This ignores phases etc.
     * It is purely to trace equipment that are connected in any way.
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun normalConnectedEquipmentTrace(): BasicTraversal<ConductingEquipment> = ConnectedEquipmentTrace.newNormalConnectedEquipmentTrace()

    /**
     * Creates a new traversal that traces equipment that are connected stopping at currently open points. This ignores phases etc.
     * It is purely to trace equipment that are connected in any way.
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun currentConnectedEquipmentTrace(): BasicTraversal<ConductingEquipment> = ConnectedEquipmentTrace.newCurrentConnectedEquipmentTrace()

    /**
     * Creates a new phase based trace ignoring the state of open phases
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun phaseTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newTrace()

    /**
     * Creates a new phase based trace stopping on normally open phases
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun normalPhaseTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newNormalTrace()

    /**
     * Creates a new phase based trace stopping at currently open phases
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun currentPhaseTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newCurrentTrace()

    /**
     * Creates a new downstream trace based on phases and the normal state of the network. Note that the phases
     * need to be set on the network before a concept of downstream is known.
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun normalDownstreamTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newNormalDownstreamTrace()

    /**
     * Creates a new downstream trace based on phases and the current state of the network. Note that the phases
     * need to be set on the network before a concept of downstream is known.
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun currentDownstreamTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newCurrentDownstreamTrace()

    /**
     * Creates a new upstream trace based on phases and the normal state of the network. Note that the phases
     * need to be set on the network before a concept of downstream is known.
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun normalUpstreamTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newNormalUpstreamTrace()

    /**
     * Creates a new upstream trace based on phases and the current state of the network. Note that the phases
     * need to be set on the network before a concept of downstream is known.
     *
     * @return The new traversal instance.
     */
    @JvmStatic
    fun currentUpstreamTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newCurrentUpstreamTrace()

    /**
     * Returns an instance of [SetPhases] convenience class for setting phases on a network.
     *
     * @return A new traversal instance.
     */
    @JvmStatic
    fun setPhases(): SetPhases = SetPhases()

    /**
     * Returns an instance of [PhaseInferrer] convenience class for inferring phases on a network.
     *
     * @return A new traversal instance.
     */
    @JvmStatic
    fun phaseInferrer(): PhaseInferrer = PhaseInferrer()

    /**
     * Returns an instance of [RemovePhases] convenience class for removing phases from a network.
     *
     * @return A new traversal instance.
     */
    @JvmStatic
    fun removePhases(): RemovePhases = RemovePhases()

    /**
     * Returns an instance of [AssignToFeeders] convenience class for assigning equipment containers to feeders on a network.
     *
     * @return A new traversal instance.
     */
    @JvmStatic
    fun assignEquipmentContainersToFeeders(): AssignToFeeders = AssignToFeeders()

    /**
     * Returns an instance of [DownstreamTree] convenience class for tracing using the
     * normal state of a network
     * .
     *
     * @return A new traversal instance.
     */
    @JvmStatic
    fun normalDownstreamTree(): DownstreamTree = DownstreamTree(OpenTest.NORMALLY_OPEN, PhaseSelector.NORMAL_PHASES)

    /**
     * Returns an instance of [DownstreamTree] convenience class for tracing using the
     * current state of a network
     *
     * @return A new traversal instance.
     */
    @JvmStatic
    fun currentDownstreamTree(): DownstreamTree = DownstreamTree(OpenTest.CURRENTLY_OPEN, PhaseSelector.CURRENT_PHASES)

    /**
     * Returns an instance of [FindWithUsagePoints] convenience class for finding conducting equipment with attached usage points.
     *
     * @return A new traversal instance.
     */
    @JvmStatic
    fun findWithUsagePoints(): FindWithUsagePoints = FindWithUsagePoints()

    /**
     * Returns a traversal that traces terminals containing a [SinglePhaseKind] from the start terminal using nominal phasing. You can add custom step
     * actions and stop conditions to the returned traversal.
     *
     * NOTE: This does not correctly support un-ganged switching and will bypass them.
     */
    @JvmStatic
    fun associatedTerminalTrace(): BasicTraversal<Terminal> = AssociatedTerminalTrace.newTrace()

    @JvmStatic
    fun normalAssociatedTerminalTrace(): BasicTraversal<Terminal> = AssociatedTerminalTrace.newNormalTrace()

    @JvmStatic
    fun currentAssociatedTerminalTrace(): BasicTraversal<Terminal> = AssociatedTerminalTrace.newCurrentTrace()
}
