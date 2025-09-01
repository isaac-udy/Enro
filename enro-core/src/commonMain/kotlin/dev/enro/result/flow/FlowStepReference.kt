package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.result.flow.FlowStepReference.Companion.getResult
import dev.enro.result.flow.FlowStepReference.Companion.setCompleted
import kotlin.jvm.JvmName

/**
 * A reference to a specific step in a [NavigationFlow] that allows manual manipulation of the step's result.
 *
 * This class is typically obtained through the [NavigationFlow.getStep] or [NavigationFlow.requireStep] functions.
 * It provides the ability to:
 * - Clear the result of the step using [clearResult]
 * - Set a result for the step using [setResult]
 * - Get the current result of the step using [getResult]
 * - Trigger editing of the step using [editStep], which clears the result and updates the flow,
 *   which will cause the flow to return to this step
 *
 * This is useful for advanced flow management scenarios where you need to programmatically control
 * the execution state of individual steps within a navigation flow.
 */
@AdvancedEnroApi
public class FlowStepReference<out T : NavigationKey>(
    private val flow: NavigationFlow<*>,
    private val resultManager: FlowResultManager,
    private val step: FlowStep<Any>,
) {
    private fun setResultUnsafe(result: Any) {
        @Suppress("UNCHECKED_CAST")
        resultManager.set(step, result)
    }

    private fun getResultUnsafe(): Any? {
        return resultManager.get(step)
    }

    /**
     * Checks whether this step has been completed.
     *
     * A step is considered completed if it has a result stored in the [FlowResultManager].
     * This can happen either through normal flow execution, or by manually setting a result
     * using [setCompleted] or [FlowStepReference.Companion.setCompleted].
     *
     * @return true if the step has a result, false otherwise
     */
    public fun isCompleted(): Boolean {
        return resultManager.get(step) != null
    }

    /**
     * Clears the result for this step.
     *
     * This won't cause the NavigationFlow to update, but next time it does update, the user will be returned to this step.
     */
    public fun clearResult() {
        resultManager.clear(step.id)
    }

    /**
     * Triggers editing of the step in the NavigationFlow. This clears the result, and immediately triggers an [update] on
     * the flow.
     *
     * If you want to cause multiple steps to be cleared before editing, you should call [clearResult] on each step before
     * calling [editStep] on the step that should be edited.
     */
    public fun editStep() {
        clearResult()
        flow.update()
    }

    public companion object Companion {
        /**
         * Gets the current result for the step, which may be null if the result has been cleared or the step has not been
         * executed yet.
         */
        public fun <R : Any> FlowStepReference<NavigationKey.WithResult<R>>.getResult(): R? {
            val result = getResultUnsafe() ?: return null
            @Suppress("UNCHECKED_CAST")
            return result as R
        }

        /**
         * Marks the step as completed for steps that do not have a result type.
         *
         * This method sets the result to [Unit], which signifies completion of the step
         * without returning any meaningful result data. It's primarily used for steps
         * that don't require a result to be passed back to the flow.
         */
        public fun FlowStepReference<*>.setCompleted() {
            setResultUnsafe(Unit)
        }

        @JvmName("setCompletedWithoutResult")
        @Deprecated(
            message = "A NavigationKey.WithResult should not be completed without a result, doing so will result in an error",
            level = DeprecationLevel.ERROR,
        )
        public fun <R : Any> FlowStepReference<NavigationKey.WithResult<R>>.setCompleted() {
            error("${step.key} is a NavigationKey.WithResult and cannot be completed without a result")
        }

        /**
         * Sets the result for this step and marks it as completed.
         *
         * This method is used for steps that have a result type ([NavigationKey.WithResult]). It stores the provided result
         * and signals completion of the step. The NavigationFlow will then proceed to the next step that
         * has not yet been completed.
         */
        public fun <R : Any> FlowStepReference<NavigationKey.WithResult<R>>.setCompleted(result: R) {
            setResultUnsafe(result)
        }
    }
}
