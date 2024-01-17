package com.zepben.evolve.services.network.tracing.networktrace

import com.zepben.evolve.cim.iec61970.base.core.PhaseCode
import com.zepben.evolve.services.network.tracing.connectivity.*
import com.zepben.evolve.services.network.tracing.feeder.*
import com.zepben.evolve.services.network.tracing.phases.*
import com.zepben.evolve.services.network.tracing.traversals.BasicQueue
import com.zepben.evolve.cim.iec61970.base.wires.Switch
import com.zepben.evolve.services.network.tracing.networktrace.conditions.terminalConnectivity
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class TracingTest {

    @Test
    fun playground() {
        val trace = Tracing.connectedEquipmentTrace<Unit>()
    }

    @Test
    fun `tracing replacements`() {
        /**
         * Here are the functions from the original Tracing.kt to create tracing functions.
         * I am trying to prove if we can replace most of these with just the `NetworkTrace` class
         */

        // fun connectedEquipmentTrace(): ConnectedEquipmentTraversal = ConnectedEquipmentTrace.newConnectedEquipmentTrace()
        Tracing.connectedEquipmentTrace<Unit>()

//        fun connectedEquipmentBreadthTrace(): ConnectedEquipmentTraversal = ConnectedEquipmentTrace.newConnectedEquipmentBreadthTrace()
        Tracing.connectedEquipmentTrace<Unit>(queue = BasicQueue.breadthFirst())

//        fun normalConnectedEquipmentTrace(): ConnectedEquipmentTraversal = ConnectedEquipmentTrace.newNormalConnectedEquipmentTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .normallyUpstream()
            .stopAtNormallyOpen()

//        fun currentConnectedEquipmentTrace(): ConnectedEquipmentTraversal = ConnectedEquipmentTrace.newCurrentConnectedEquipmentTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .stopAtCurrentlyOpen()

        /*
         TODO: Investigate more how the limited trace work. I don't think what I've done matches what is there,
               but I'm hoping we can do something like what I've done here
         */
//        fun normalLimitedConnectedEquipmentTrace(): LimitedConnectedEquipmentTrace = ConnectedEquipmentTrace.newNormalLimitedConnectedEquipmentTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .stopAtNormallyOpen()
            .limitEquipmentSteps(10)


//        fun currentLimitedConnectedEquipmentTrace(): LimitedConnectedEquipmentTrace = ConnectedEquipmentTrace.newCurrentLimitedConnectedEquipmentTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .stopAtCurrentlyOpen()
            .limitEquipmentSteps(10, Switch::class.java) // If you want to limit to 10 switches

//        fun normalDownstreamEquipmentTrace(queue: TraversalQueue<ConductingEquipment> = BasicQueue.depthFirst()): BasicTraversal<ConductingEquipment> =
//            ConnectedEquipmentTrace.newNormalDownstreamEquipmentTrace(queue)
        Tracing.connectedEquipmentTrace<Unit>()
            .normallyDownstream()

//        fun currentDownstreamEquipmentTrace(queue: TraversalQueue<ConductingEquipment> = BasicQueue.depthFirst()): BasicTraversal<ConductingEquipment> =
//            ConnectedEquipmentTrace.newCurrentDownstreamEquipmentTrace(queue)
        Tracing.connectedEquipmentTrace<Unit>()
            .currentlyDownstream()

//        fun normalUpstreamEquipmentTrace(queue: TraversalQueue<ConductingEquipment> = BasicQueue.depthFirst()): BasicTraversal<ConductingEquipment> =
//            ConnectedEquipmentTrace.newNormalUpstreamEquipmentTrace(queue)
        Tracing.connectedEquipmentTrace<Unit>()
            .normallyUpstream()

//        fun currentUpstreamEquipmentTrace(queue: TraversalQueue<ConductingEquipment> = BasicQueue.depthFirst()): BasicTraversal<ConductingEquipment> =
//            ConnectedEquipmentTrace.newCurrentUpstreamEquipmentTrace(queue)
        Tracing.connectedEquipmentTrace<Unit>()
            .currentlyUpstream()

        // NOTE: The new phase tracing doesn't map 1 to 1 to what was previously there. When we reviewed the
        //       difference between 'connectivity trace' and 'phase trace' and their use cases we felt they
        //       were not needed and just made things confusing.
        //       We are hoping the new phase tracing covers existing use cases as it is easier to understand.

//        fun connectivityTrace(): BasicTraversal<ConnectivityResult> = ConnectivityTrace.newConnectivityTrace()
//        fun phaseTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .withPhases(PhaseCode.ABCN)
            .addStepAction { step, ctx ->
                // This is the ConnectivityResult you used to get at the step item in the ConnectivityTrace
                val connectivityResult = ctx.terminalConnectivity()
            }

//        fun connectivityBreadthTrace(): BasicTraversal<ConnectivityResult> = ConnectivityTrace.newConnectivityBreadthTrace()
        Tracing.connectedEquipmentTrace<Unit>(BasicQueue.breadthFirst())
            .withPhases(PhaseCode.ABCN)

//        fun normalConnectivityTrace(): BasicTraversal<ConnectivityResult> = ConnectivityTrace.newNormalConnectivityTrace()
//        fun normalPhaseTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newNormalTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .stopAtNormallyOpen()
            .withPhases(PhaseCode.ABCN)

//        fun currentConnectivityTrace(): BasicTraversal<ConnectivityResult> = ConnectivityTrace.newCurrentConnectivityTrace()
//        fun currentPhaseTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newCurrentTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .stopAtCurrentlyOpen()
            .withPhases(PhaseCode.ABCN)

//        fun normalDownstreamTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newNormalDownstreamTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .withPhases(PhaseCode.ABCN)
            .normallyDownstream()

//        fun currentDownstreamTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newCurrentDownstreamTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .withPhases(PhaseCode.ABCN)
            .currentlyDownstream()

//        fun normalUpstreamTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newNormalUpstreamTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .withPhases(PhaseCode.ABCN)
            .normallyUpstream()

//        fun currentUpstreamTrace(): BasicTraversal<PhaseStep> = PhaseTrace.newCurrentUpstreamTrace()
        Tracing.connectedEquipmentTrace<Unit>()
            .withPhases(PhaseCode.ABCN)
            .currentlyUpstream()

        /**
         * Need to look at the rest of these.
         * Have not yet looked at BranchRecursiveTraversal which will be needed for some of these.
         */
//        fun setPhases(): SetPhases = SetPhases()

//        fun setDirection(): SetDirection = SetDirection()

//        fun phaseInferrer(): PhaseInferrer = PhaseInferrer()

//        fun removePhases(): RemovePhases = RemovePhases()

//        fun removeDirection(): RemoveDirection = RemoveDirection()

//        fun assignEquipmentToFeeders(): AssignToFeeders = AssignToFeeders()

//        fun assignEquipmentToLvFeeders(): AssignToLvFeeders = AssignToLvFeeders()

//        fun normalDownstreamTree(): DownstreamTree = DownstreamTree(OpenTest.NORMALLY_OPEN, DirectionSelector.NORMAL_DIRECTION)

//        fun currentDownstreamTree(): DownstreamTree = DownstreamTree(OpenTest.CURRENTLY_OPEN, DirectionSelector.CURRENT_DIRECTION)

//        fun findWithUsagePoints(): FindWithUsagePoints = FindWithUsagePoints()
    }
}