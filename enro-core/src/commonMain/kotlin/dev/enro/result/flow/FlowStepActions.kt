package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import kotlin.jvm.JvmName

@AdvancedEnroApi
public class FlowStepActions<T : NavigationKey>(
    private val flow: NavigationFlow<*>,
    private val resultManager: FlowResultManager,
    private val step: FlowStep<out Any>,
) {
    private fun setResultUnsafe(result: Any) {
        @Suppress("UNCHECKED_CAST")
        resultManager
            .set(step as FlowStep<Any>, result)
    }

    private fun getResultUnsafe(): Any? {
        return resultManager
            .get(step)
    }

    /**
     * Clears the result for this step.
     *
     * This won't cause the NavigationFlow to update, but next time it does update, the user will be returned to this step.
     */
    public fun clearResult() {
        resultManager
            .clear(step.stepId)
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

    public companion object {
        /**
         * Sets a result for the step
         */
        public fun <R : Any> FlowStepActions<out NavigationKey.WithResult<R>>.setResult(result: R) {
            setResultUnsafe(result)
        }

        /**
         * Gets the current result for the step, which may be null if the result has been cleared or the step has not been
         * executed yet.
         */
        public fun <R : Any> FlowStepActions<out NavigationKey.WithResult<R>>.getResult(): R? {
            val result = getResultUnsafe() ?: return null
            @Suppress("UNCHECKED_CAST")
            return result as R
        }
    }
}

@AdvancedEnroApi
@JvmName("getStepTyped")
public inline fun <reified T : NavigationKey.WithResult<*>> NavigationFlow<*>.getStep(
    block: (T) -> Boolean = { true },
): FlowStepActions<T>? {
    return getSteps()
        .firstOrNull {
            it.key is T && block(it.key)
        }
        ?.let {
            FlowStepActions(this, getResultManager(), it)
        }
}

@AdvancedEnroApi
public fun NavigationFlow<*>.getStep(
    block: (NavigationKey) -> Boolean = { true },
): FlowStepActions<NavigationKey>? {
    return getSteps()
        .firstOrNull {
            block(it.key)
        }
        ?.let {
            FlowStepActions(this, getResultManager(), it)
        }
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey.WithResult<*>> NavigationFlow<*>.requireStep(
    block: (T) -> Boolean = { true },
): FlowStepActions<T> {
    return requireNotNull(getStep(block))
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey.WithResult<*>> NavigationFlowScope.getStep(
    block: (T) -> Boolean = { true },
): FlowStepActions<T>? {
    return steps
        .firstOrNull {
            it.key is T && block(it.key)
        }
        ?.let {
            FlowStepActions(flow, resultManager, it)
        }
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey.WithResult<*>> NavigationFlowScope.requireStep(
    block: (T) -> Boolean = { true },
): FlowStepActions<T> {
    return requireNotNull(getStep(block))
}