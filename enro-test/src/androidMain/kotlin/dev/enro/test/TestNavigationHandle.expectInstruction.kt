package dev.enro.test

import dev.enro.core.NavigationInstruction

@Deprecated("Use assertClosed instead")
fun TestNavigationHandle<*>.expectCloseInstruction() {
    assertClosed()
}

/**
 * Asserts that the NavigationHandle has received a NavigationInstruction with a NavigationKey that is assignable to type [T] and
 * which matches the provided filter, and then returns that NavigationInstruction.
 */
@Deprecated("Use assertAnyInstructionOpened instead")
fun <T : Any> TestNavigationHandle<*>.expectOpenInstruction(
    type: Class<T>,
    filter: (T) -> Boolean = { true }
): NavigationInstruction.Open<*> {
    val openInstructions = instructions.filterIsInstance<NavigationInstruction.Open<*>>()
    if (openInstructions.isEmpty()) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open")
    }
    val instructionsWithCorrectType = openInstructions.filter {
        type.isAssignableFrom(it.navigationKey::class.java)
    }
    if (instructionsWithCorrectType.isEmpty()) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open with a NavigationKey of type $type")
    }
    val instruction = instructionsWithCorrectType.lastOrNull {
        runCatching {
            @Suppress("UNCHECKED_CAST")
            filter(it.navigationKey as T)
        }.getOrDefault(false)
    }
    if (instruction == null) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open with a NavigationKey of type $type that matches the provided filter")
    }
    return instruction
}

/**
 * Asserts that the NavigationHandle has received a NavigationInstruction with a NavigationKey that is assignable to type [T] and
 * which matches the provided filter, and then returns that NavigationInstruction.
 */
@Deprecated("Use assertAnyInstructionOpened instead")
inline fun <reified T : Any> TestNavigationHandle<*>.expectOpenInstruction(noinline filter: (T) -> Boolean = { true }): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java, filter)
}

/**
 * Asserts that the NavigationHandle has received a NavigationInstruction with a NavigationKey that is equal to the provided
 * NavigationKey [key], and then returns that NavigationInstruction.
 */
@Deprecated("Use assertAnyInstructionOpened instead")
inline fun <reified T : Any> TestNavigationHandle<*>.expectOpenInstruction(key: T): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java) { it == key }
}