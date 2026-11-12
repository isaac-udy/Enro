package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.result.flow.FlowStepReference.Companion.getResult
import dev.enro.result.flow.FlowStepReference.Companion.setCompleted
import kotlin.jvm.JvmName

/**
 * A reference to one step of a [NavigationFlow]. A [FlowStepReference] reads, sets and clears
 * the result of the step. [NavigationFlow.getStep] and [NavigationFlow.requireStep] return a
 * [FlowStepReference].
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
     * Returns true if the [FlowResultManager] has a result for this step. The result is set by
     * the destination of the step or by [setCompleted].
     *
     * [isCompleted] discards a result whose dependencies have changed and returns false.
     */
    public fun isCompleted(): Boolean {
        return resultManager.get(step) != null
    }

    /**
     * Clears the result of this step without updating the [NavigationFlow]. The next
     * [NavigationFlow.update] shows this step again.
     */
    public fun clearResult() {
        resultManager.clear(step.id)
    }

    /**
     * Clears the result of this step, then calls [NavigationFlow.update], which shows this step.
     *
     * To clear more than one step, call [clearResult] on each other step before calling [editStep].
     */
    public fun editStep() {
        clearResult()
        flow.update()
    }

    public companion object Companion {
        /**
         * Returns the result of the step, or null if the step has no result or the result was cleared.
         */
        public fun <R : Any> FlowStepReference<NavigationKey.WithResult<R>>.getResult(): R? {
            val result = getResultUnsafe() ?: return null
            @Suppress("UNCHECKED_CAST")
            return result as R
        }

        /**
         * Marks a step without a result type as completed. Does not update the [NavigationFlow]:
         * call [NavigationFlow.update] to show the next step.
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
         * Sets [result] as the result of the step. Does not update the [NavigationFlow]: call
         * [NavigationFlow.update] to show the next step that has no result.
         */
        public fun <R : Any> FlowStepReference<NavigationKey.WithResult<R>>.setCompleted(result: R) {
            setResultUnsafe(result)
        }
    }
}
