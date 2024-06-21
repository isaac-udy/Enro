package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.container.NavigationContainerContext

/**
 * Asserts that the NavigationContainerContext's backstack contains a NavigationInstruction with a NavigationKey of type [T]
 * that matches the provided filter, and then returns that NavigationInstruction
 */
fun <T : Any> NavigationContainerContext.expectOpenInstruction(
    type: Class<T>,
    filter: (T) -> Boolean = { true }
): NavigationInstruction.Open<*> {
    if (backstack.isEmpty()) {
        enroAssertionError("NavigationContainer's backstack is empty")
    }
    val assignableInstructions = backstack.filter {
        type.isAssignableFrom(it.navigationKey::class.java)
    }
    if (assignableInstructions.isEmpty()) {
        enroAssertionError("NavigationContainer had no NavigationInstructions with a NavigationKey of type $type\n\tBackstack: $backstack")
    }
    val instruction = assignableInstructions.lastOrNull {
        runCatching { filter(it.navigationKey as T) }.getOrDefault(false)
    }
    if (instruction == null) {
        enroAssertionError("NavigationContainer had NavigationInstructions with NavigationKey of type $type, but none matched the provided filter\n\tBackstack: $backstack")
    }
    return instruction
}

/**
 * Asserts that the NavigationContainerContext's backstack contains a NavigationInstruction with a NavigationKey of type [T]
 * that matches the provided filter, and then returns that NavigationInstruction
 */
inline fun <reified T : Any> NavigationContainerContext.expectOpenInstruction(noinline filter: (T) -> Boolean = { true }): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java, filter)
}

/**
 * Asserts that the NavigationContainerContext's backstack contains a NavigationInstruction with a NavigationKey
 * that is equal to the provided key, and then returns that NavigationInstruction
 */
inline fun <reified T : Any> NavigationContainerContext.expectOpenInstruction(key: T): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java) { it == key }
}