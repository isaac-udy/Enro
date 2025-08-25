package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import kotlin.reflect.KClass

@Deprecated("Use assertClosed instead")
fun TestNavigationHandle<*>.expectCloseInstruction() {
    @Suppress("UNCHECKED_CAST")
    this as TestNavigationHandle<NavigationKey>
    assertClosed()
}
@Deprecated("Use assertOpened instead")
fun <T : NavigationKey> TestNavigationHandle<*>.expectOpenInstruction(
    type: Class<T>,
    filter: (T) -> Boolean = { true }
) {
    expectOpenInstruction(type.kotlin, filter)
}

/**
 * Asserts that the NavigationHandle has received a NavigationInstruction with a NavigationKey that is assignable to type [T] and
 * which matches the provided filter, and then returns that NavigationInstruction.
 */
@Deprecated("Use assertOpened instead")
fun <T : NavigationKey> TestNavigationHandle<*>.expectOpenInstruction(
    type: KClass<T>,
    filter: (T) -> Boolean = { true }
): NavigationKey.Instance<T> {
    val openInstructions = operations.filterIsInstance<NavigationOperation.Open<*>>()
    if (openInstructions.isEmpty()) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open")
    }
    val instructionsWithCorrectType = openInstructions.filter {
        type.isInstance(it.instance.key)
    }
    if (instructionsWithCorrectType.isEmpty()) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open with a NavigationKey of type $type")
    }
    val instruction = instructionsWithCorrectType.lastOrNull {
        runCatching {
            @Suppress("UNCHECKED_CAST")
            filter(it.instance.key as T)
        }.getOrDefault(false)
    }
    if (instruction == null) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open with a NavigationKey of type $type that matches the provided filter")
    }
    @Suppress("UNCHECKED_CAST")
    return instruction.instance as NavigationKey.Instance<T>
}


/**
 * Asserts that the NavigationHandle has received a NavigationInstruction with a NavigationKey that is equal to the provided
 * NavigationKey [key], and then returns that NavigationInstruction.
 */
@Deprecated("Use assertOpened instead")
inline fun <reified T : NavigationKey> TestNavigationHandle<*>.expectOpenInstruction(key: T): NavigationKey.Instance<T> {
    return expectOpenInstruction(T::class) { it == key }
}

/**
 * Asserts that the NavigationHandle has received a NavigationInstruction with a NavigationKey that is assignable to type [T] and
 * which matches the provided filter, and then returns that NavigationInstruction.
 */
@Deprecated("Use assertOpened instead")
inline fun <reified T : NavigationKey> TestNavigationHandle<*>.expectOpenInstruction(noinline filter: (T) -> Boolean = { true }): NavigationKey.Instance<T> {
    return expectOpenInstruction(T::class, filter)
}
