package dev.enro.core.result.flows

import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.NavigationKey

@AdvancedEnroApi
public class FlowStepActions<T: NavigationKey.WithResult<*>>(
    private val resultManager: FlowResultManager,
    private val step: FlowStep<out Any>
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

    public fun clearResult() {
        resultManager
            .clear(step)
    }

    public companion object {
        public fun <R : Any> FlowStepActions<out NavigationKey.WithResult<in R>>.setResult(result: R) {
            setResultUnsafe(result)
        }

        public fun <R : Any> FlowStepActions<out NavigationKey.WithResult<out R>>.getResult(): R? {
            val result = getResultUnsafe() ?: return null
            @Suppress("UNCHECKED_CAST")
            return result as R
        }
    }
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey.WithResult<*>> NavigationFlow<*>.getStep(
    block: (T) -> Boolean = { true }
) : FlowStepActions<T>? {
    return getSteps()
        .firstOrNull {
            it.key is T && block(it.key)
        }
        ?.let {
            FlowStepActions(getResultManager(), it)
        }
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey.WithResult<*>> NavigationFlow<*>.requireStep(
    block: (T) -> Boolean = { true }
) : FlowStepActions<T> {
    return requireNotNull(getStep(block))
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey.WithResult<*>> NavigationFlowScope.getStep(
    block: (T) -> Boolean = { true }
) : FlowStepActions<T>? {
    return steps
        .firstOrNull {
            it.key is T && block(it.key)
        }
        ?.let {
            FlowStepActions(resultManager, it)
        }
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey.WithResult<*>> NavigationFlowScope.requireStep(
    block: (T) -> Boolean = { true }
) : FlowStepActions<T> {
    return requireNotNull(getStep(block))
}