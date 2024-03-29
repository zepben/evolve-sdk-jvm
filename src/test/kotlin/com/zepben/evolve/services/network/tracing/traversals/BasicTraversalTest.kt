/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.tracing.traversals

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import java.util.function.Consumer

internal class BasicTraversalTest {

    private val queueNext = BasicTraversal.QueueNext<Int> { i, t ->
        sequenceOf(i - 2, i - 1, i + 1, i + 2)
            .filter { n -> n > 0 }
            .forEach { item -> t.queue.add(item) }
    }

    @Test
    internal fun testBreadthFirst() {
        val expectedOrder = listOf(1, 2, 3, 4, 5, 6, 7)
        val visitOrder = mutableListOf<Int>()

        val t = BasicTraversal(queueNext, BasicQueue.breadthFirst(), BasicTracker())
            .addStopCondition { i -> i >= 6 }
            .addStepAction { i, _ -> visitOrder.add(i) }

        validateRun(t, true, visitOrder, expectedOrder)
    }

    @Test
    internal fun testDepthFirst() {
        val expectedOrder = listOf(1, 3, 5, 7, 6, 4, 2)
        val visitOrder = mutableListOf<Int>()

        val t = BasicTraversal(queueNext, BasicQueue.depthFirst(), BasicTracker())
            .addStopCondition { i -> i >= 6 }
            .addStepAction { i, _ -> visitOrder.add(i) }

        validateRun(t, true, visitOrder, expectedOrder)
    }

    @Test
    internal fun canControlStoppingOnFirstAsset() {
        validateStoppingOnFirstAsset(BasicTraversal(queueNext, BasicQueue.breadthFirst(), BasicTracker()), listOf(1, 2, 3))
        validateStoppingOnFirstAsset(BasicTraversal(queueNext, BasicQueue.depthFirst(), BasicTracker()), listOf(1, 3, 2))
    }

    @Test
    internal fun passesStoppingToStep() {
        val queueNext = BasicTraversal.QueueNext<Int> { i, t ->
            t.queue.add(i + 1)
            t.queue.add(i + 2)
        }

        val visited = mutableSetOf<Int>()
        val stoppingOn = mutableSetOf<Int>()

        val t = BasicTraversal(queueNext, BasicQueue.depthFirst(), BasicTracker())
            .addStopCondition { i -> i >= 3 }
            .addStepAction { i, s ->
                visited.add(i)
                if (s)
                    stoppingOn.add(i)
            }

        t.run(1)
        assertThat(visited, containsInAnyOrder(1, 2, 3, 4))
        assertThat(stoppingOn, containsInAnyOrder(3, 4))
    }

    @Test
    internal fun runsAllStopChecks() {
        val stopCalls = mutableListOf(0, 0, 0)

        BasicTraversal<Int>({ _, _ -> }, BasicQueue.depthFirst(), BasicTracker())
            .addStopCondition { i -> stopCalls[0] = i; true }
            .addStopCondition { i -> stopCalls[1] = i; true }
            .addStopCondition { i -> stopCalls[2] = i; true }
            .run(1)

        assertThat(stopCalls, contains(1, 1, 1))
    }

    @Test
    internal fun runsAllStepActions() {
        val stopCalls = mutableListOf(0, 0, 0)

        BasicTraversal<Int>({ _, _ -> }, BasicQueue.depthFirst(), BasicTracker())
            .addStepAction { i, _ -> stopCalls[0] = i }
            .addStepAction(Consumer { i -> stopCalls[1] = i })
            .addStepAction { i -> stopCalls[2] = i }
            .run(1)

        assertThat(stopCalls, contains(1, 1, 1))
    }

    @Test
    internal fun `stop checking actions are triggered correctly`() {
        // We do not bother with the queue next as we will just prime the queue with what we want to test.
        val queueNext = BasicTraversal.QueueNext<Int> { _, _ -> }

        val steppedOn = mutableSetOf<Int>()
        val notStoppingOn = mutableSetOf<Int>()
        val stoppingOn = mutableSetOf<Int>()

        BasicTraversal(queueNext, BasicQueue.depthFirst(), BasicTracker()).apply {
            addStopCondition { it >= 3 }
            addStepAction { steppedOn.add(it) }
            ifNotStopping { notStoppingOn.add(it) }
            ifStopping { stoppingOn.add(it) }

            queue.addAll(1, 2, 3, 4)

            run()
        }

        assertThat(steppedOn, containsInAnyOrder(1, 2, 3, 4))
        assertThat(notStoppingOn, containsInAnyOrder(1, 2))
        assertThat(stoppingOn, containsInAnyOrder(3, 4))
    }

    private fun validateStoppingOnFirstAsset(t: BasicTraversal<Int>, expectedOrder: List<Int>) {
        t.addStopCondition { i -> i >= 0 }
        t.addStopCondition { i -> i >= 6 }

        val visitOrder = mutableListOf<Int>()
        t.addStepAction { i, _ -> visitOrder.add(i) }

        validateRun(t, false, visitOrder, expectedOrder)

        t.reset()
        visitOrder.clear()

        validateRun(t, true, visitOrder, listOf(1))
    }

    private fun validateRun(t: Traversal<Int>, canStopOnStart: Boolean, visitOrder: List<Int>, expectedOrder: List<Int>) {
        t.run(1, canStopOnStart)
        assertThat(visitOrder, contains<Any>(*expectedOrder.toTypedArray()))
        expectedOrder.forEach { assertThat("Tracker should have visited $it", t.tracker.hasVisited(it)) }
    }

}
