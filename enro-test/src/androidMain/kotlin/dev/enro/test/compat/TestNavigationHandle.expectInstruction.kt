package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import kotlin.reflect.KClass

@Deprecated("Use assertClosed instead")
public fun TestNavigationHandle<*>.expectCloseInstruction() {
    @Suppress("UNCHECKED_CAST")
    this as TestNavigationHandle<NavigationKey>
    assertClosed()
}
@Deprecated("Use assertOpened instead")
public fun <T : NavigationKey> TestNavigationHandle<*>.expectOpenInstruction(
    type: Class<T>,
    filter: (T) -> Boolean = { true }
) {
    expectOpenInstruction(type.kotlin, filter)
}

/**
 * Asserts that the handle executed an Open for a NavigationKey of [type] that matches
 * [filter]. Returns the instance of the last matching Open.
 */
@Deprecated("Use assertOpened instead")
public fun <T : NavigationKey> TestNavigationHandle<*>.expectOpenInstruction(
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
 * Asserts that the handle executed an Open for a NavigationKey equal to [key]. Returns the
 * instance of the last matching Open.
 */
@Deprecated("Use assertOpened instead")
public fun <T : NavigationKey> TestNavigationHandle<*>.expectOpenInstruction(
    key: T,
    disambiguation: Unit = Unit // distinguishes this overload from expectOpenInstruction(filter)
): NavigationKey.Instance<T> {
    return expectOpenInstruction(key::class) { it == key }
}

@Deprecated("Use assertOpened instead")
public inline fun <reified T : NavigationKey> TestNavigationHandle<*>.expectOpenInstruction(
    noinline filter: (T) -> Boolean = { true }
): NavigationKey.Instance<T> {
    return expectOpenInstruction(T::class, filter)
}
