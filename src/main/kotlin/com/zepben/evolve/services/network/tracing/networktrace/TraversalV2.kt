/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network.tracing.traversals

import com.zepben.evolve.services.network.tracing.networktrace.StepContext
import java.util.*
import java.util.function.Consumer

/**
 *
 * Base class that provides some common functionality for traversals. This includes things like registering callbacks
 * to be called at every step in the traversal as well as registering stop conditions that traversals can check for when
 * to stop following a path.
 *
 * This base class does not actually provide any way to traverse the items. It needs to be implemented in
 * subclasses. See [BasicTraversal] for an example.
 *
 * Note this class is not thread safe!
 *
 * @param T Object type to be traversed.
 */
abstract class TraversalV2<T, D : TraversalV2<T, D>>(
    private val queueNext: QueueNext<T, D>,
    private val queue: TraversalQueue<T>,
    val tracker: Tracker<T>
) {
    fun interface ComputeContext<T> {
        fun compute(item: T, key: String, context: StepContext): Any?
    }

    /**
     * Represents a consumer that takes the current item of the traversal, and the traversal instance so items can be queued.
     *
     * @param T The type of object being traversed.
     */
    fun interface QueueNext<T, D : TraversalV2<T, D>> {
        fun accept(item: T, context: StepContext, traversal: D)
    }

    fun interface StepAction<T> {

        fun apply(item: T, context: StepContext)

    }

    /**
     * The item the traversal will start at, or `null` if it has not been set.
     */
    var startItem: T? = null

    @Volatile
    private var running = false

    @Volatile
    private var hasRun = false
    private val stopConditions = mutableListOf<(T, StepContext) -> Boolean>()
    private val queueConditions = mutableListOf<(T, StepContext) -> Boolean>()
    private val stepActions = mutableListOf<StepAction<T>>()

    private val computeNextContextFuns: MutableMap<String, ComputeContext<T>> = mutableMapOf()
    protected val contexts: IdentityHashMap<T, StepContext> = IdentityHashMap()

    protected abstract fun getDerivedThis(): D

    /**
     *
     * Add a callback to check whether the current item in the traversal is a stop point.
     *
     * If any of the registered stop conditions return true, the traversal will not call the callback to queue more items.
     * Note that a match on a stop condition doesn't necessarily stop the traversal, it just stops
     * traversal of the current branch.
     *
     * @param condition A predicate that if returns true will cause the traversal to stop traversing the branch.
     * @return this traversal instance.
     */
    fun addStopCondition(condition: (T, StepContext) -> Boolean): D {
        stopConditions.add(condition)
        return getDerivedThis()
    }

    /**
     * Clears all of the stop conditions registered on this traversal.
     */
    fun clearStopConditions(): D {
        stopConditions.clear()
        return getDerivedThis()
    }

    /**
     * Copies all the stop conditions from another traversal to this traversal
     *
     * @param other The other traversal object to copy from.
     * @return this traversal instance.
     */
    fun copyStopConditions(other: TraversalV2<T, D>): D {
        stopConditions.addAll(other.stopConditions)
        return getDerivedThis()
    }

    /**
     * Checks all the stop conditions for the passed in item and returns true if any match.
     * This calls all registered stop conditions even if one has already returned true to make sure everything is
     * notified about this item.
     *
     * @param item The item to pass to the stop conditions.
     * @return true if any of the stop conditions return true.
     */
    fun matchesAnyStopCondition(item: T, context: StepContext): Boolean =
        stopConditions.fold(false) { stop, condition -> stop or condition(item, context) }


    fun addQueueCondition(condition: (T, StepContext) -> Boolean): D {
        queueConditions.add(condition)
        return getDerivedThis()
    }

    fun clearQueueConditions(): D {
        queueConditions.clear()
        return getDerivedThis()
    }

    fun copyQueueConditions(other: TraversalV2<T, D>): D {
        queueConditions.addAll(other.queueConditions)
        return getDerivedThis()
    }

    fun matchesAllQueueConditions(item: T, context: StepContext): Boolean = queueConditions.all { it(item, context) }


    /**
     * Add a callback which is called for every item in the traversal (including the starting item).
     *
     * @param action Action to be called on each item in the traversal, passing if the trace will stop on this step.
     * @return this traversal instance.
     */
    fun addStepAction(action: StepAction<T>): D {
        stepActions.add(action)
        return getDerivedThis()
    }

    /**
     * Add a callback which is called for every item in the traversal (including the starting item).
     *
     * @param action Action to be called on each item in the traversal, without passing if the trace will stop on this step.
     * @return this traversal instance.
     */
    fun addStepAction(action: Consumer<T>): D {
        stepActions.add { it, _ -> action.accept(it) }
        return getDerivedThis()
    }

    /**
     * Add a callback which is called for every item in the traversal (including the starting item).
     *
     * @param action Action to be called on each item in the traversal, without passing if the trace will stop on this step.
     * @return this traversal instance.
     */
    fun addStepAction(action: (item: T) -> Unit): D {
        stepActions.add { it, _ -> action(it) }
        return getDerivedThis()
    }

    /**
     * Add a callback which is called for every item in the traversal that does not match a stop condition (including the starting item).
     *
     * @param action Action to be called on each item in the traversal that is not being stopped on.
     * @return this traversal instance.
     */
    fun ifNotStopping(action: (item: T, context: StepContext) -> Unit): D {
        stepActions.add { it, context -> if (!context.isStopping) action(it, context) }
        return getDerivedThis()
    }

    /**
     * Add a callback which is called for every item in the traversal that matches a stop condition (including the starting item).
     *
     * @param action Action to be called on each item in the traversal that is being stopped on.
     * @return this traversal instance.
     */
    fun ifStopping(action: (item: T, context: StepContext) -> Unit): D {
        stepActions.add { it, context -> if (context.isStopping) action(it, context) }
        return getDerivedThis()
    }

    /**
     * Clears all step actions registered on this traversal.
     */
    fun clearStepActions(): D {
        stepActions.clear()
        return getDerivedThis()
    }

    /**
     * Copies all the step actions from the passed in traversal to this traversal.
     *
     * @param other The other traversal object to copy from.
     * @return this traversal instance.
     */
    fun copyStepActions(other: TraversalV2<T, D>): D {
        stepActions.addAll(other.stepActions)
        return getDerivedThis()
    }

    /**
     * Calls all the step actions with the passed in item.
     *
     * @param item       The item to pass to the step actions.
     * @param isStopping Indicates if the trace will stop on this step.
     */
    private fun applyStepActions(item: T, context: StepContext): D {
        stepActions.forEach { it.apply(item, context) }
        return getDerivedThis()
    }

    fun addComputeNextContext(key: String, compute: ComputeContext<T>?): D {
        if (compute == null)
            computeNextContextFuns.remove(key)
        else
            computeNextContextFuns[key] = compute

        return getDerivedThis()
    }

    private fun computeNextContext(nextStep: T, context: StepContext): StepContext {
        var newContextData: MutableMap<String, Any?>? = null

        for ((key, computer) in computeNextContextFuns) {
            newContextData = newContextData ?: mutableMapOf()
            newContextData[key] = computer.compute(nextStep, key, context)
        }

        return StepContext(context.stepNumber + 1, newContextData)
    }

    /**
     * Sets the item the traversal will start at.
     *
     * @param item The item to start at.
     * @return this traversal instance.
     */
    fun setStart(item: T): D {
        startItem = item
        return getDerivedThis()
    }

    /**
     * Starts the traversal calling [setStart] on the [start] parameter before running.
     *
     * @param start              The item to start at.
     * @param canStopOnStartItem Indicates if the traversal will check the start item for stop conditions.
     */
    @JvmOverloads
    fun run(start: T, canStopOnStartItem: Boolean = true) {
        setStart(start)
        run(canStopOnStartItem)
    }

    /**
     * Starts the traversal. [setStart] should of been called to set the starting item or use the
     * overloaded run method that takes an item to start at.
     *
     * @param canStopOnStartItem Indicates if the traversal will check the start item for stop conditions.
     */
    @JvmOverloads
    fun run(canStopOnStartItem: Boolean = true) {
        check(!running) { "Traversal is already running." }
        check(!hasRun) { "Traversal must be reset before reuse." }

        running = true
        hasRun = true

        doRun(canStopOnStartItem)

        running = false
    }

    private fun resetRunFlag() {
        check(!running) { "Traversal is currently running." }
        hasRun = false
    }

    fun reset(): D {
        resetRunFlag()

        queue.clear()
        tracker.clear()

        return getDerivedThis()
    }

    private fun doRun(canStopOnStartItem: Boolean) {
        var canStop = true

        startItem?.let {
            queue.add(it)
            canStop = canStopOnStartItem
            contexts[it] = computeNextContext(it, StepContext())
        }

        while (queue.hasNext()) {
            queue.next()?.let { current ->
                if (tracker.visit(current)) {
                    val context = contexts[current] ?: error { "INTERNAL ERROR: Traversal item should always have a context" }
                    context.isStopping = canStop && matchesAnyStopCondition(current, context)

                    applyStepActions(current, context)

                    if (!context.isStopping)
                        queueNext.accept(current, context, getDerivedThis())

                    canStop = true
                }
            }
        }
    }

    fun queueItem(nextItem: T, currentContext: StepContext): Boolean {
        val queued = if (matchesAllQueueConditions(nextItem, currentContext))
            queue.add(nextItem)
        else
            false

        if (queued) {
            contexts[nextItem] = computeNextContext(nextItem, currentContext)
        }

        return queued
    }

}
