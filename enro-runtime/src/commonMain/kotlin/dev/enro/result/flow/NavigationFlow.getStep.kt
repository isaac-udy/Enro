package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import kotlin.jvm.JvmName

@AdvancedEnroApi
public inline fun <reified T : NavigationKey> NavigationFlow<*>.getStep(
    id: FlowStep.Id<T>,
): FlowStepReference<T>? {
    return getSteps()
        .firstOrNull {
            it.key is T && it.id.value == id.value
        }
        ?.let {
            FlowStepReference(this, getResultManager(), it)
        }
}

@AdvancedEnroApi
@JvmName("getStepTyped")
public inline fun <reified T : NavigationKey> NavigationFlow<*>.getStep(
    block: (T) -> Boolean = { true },
): FlowStepReference<T>? {
    return getSteps()
        .firstOrNull {
            it.key is T && block(it.key)
        }
        ?.let {
            FlowStepReference(this, getResultManager(), it)
        }
}

@AdvancedEnroApi
public fun NavigationFlow<*>.getStep(
    block: (NavigationKey) -> Boolean = { true },
): FlowStepReference<NavigationKey>? {
    return getSteps()
        .firstOrNull {
            block(it.key)
        }
        ?.let {
            FlowStepReference(this, getResultManager(), it)
        }
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey> NavigationFlow<*>.requireStep(
    block: (T) -> Boolean = { true },
): FlowStepReference<T> {
    return requireNotNull(getStep(block))
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey> NavigationFlowScope.getStep(
    block: (T) -> Boolean = { true },
): FlowStepReference<T>? {
    return steps
        .firstOrNull {
            it.key is T && block(it.key)
        }
        ?.let {
            FlowStepReference(flow, resultManager, it)
        }
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey> NavigationFlowScope.requireStep(
    block: (T) -> Boolean = { true },
): FlowStepReference<T> {
    return requireNotNull(getStep(block))
}

@AdvancedEnroApi
public inline fun <reified T : NavigationKey> NavigationFlow<*>.requireStep(
    id: FlowStep.Id<T>,
): FlowStepReference<T> {
    return requireNotNull(getStep(id))
}
