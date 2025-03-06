package dev.enro.test

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey

/**
 * This method asserts that the last navigation instruction the NavigationHandle executed was a NavigationInstruction.Open
 * with a NavigationKey of the provided type [T] and direction (if the direction parameter was not null).
 *
 * If you want to assert that any NavigationInstruction.Open was executed, and don't care whether the instruction was the
 * last instruction or not, use [assertAnyInstructionOpened].
 */
fun <T : NavigationKey> TestNavigationHandle<*>.assertInstructionOpened(
    type: Class<T>,
    direction: NavigationDirection? = null,
    predicate: (T) -> Boolean = { true }
): NavigationInstruction.Open<*> {
    val openInstructions = instructions.filterIsInstance<NavigationInstruction.Open<*>>()
    if (openInstructions.isEmpty()) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open")
    }

    val instruction = openInstructions.last()
    type.isAssignableFrom(instruction.navigationKey::class.java).shouldBeEqualTo(true) {
        "NavigationHandle was expected to have executed a NavigationInstruction.Open with a NavigationKey of type $type, but the NavigationKey was of type ${instruction.navigationKey::class.java}"
    }
    if (direction != null) {
        instruction.navigationDirection.shouldBeEqualTo(direction) {
            "NavigationHandle was expected to have executed a NavigationInstruction.Open with a NavigationDirection of $direction, but the NavigationDirection was ${instruction.navigationDirection}"
        }
    }
    instruction.navigationKey
        .shouldBeInstanceOf(type)
        .shouldMatchPredicate(predicate) {
            "NavigationHandle was expected to have executed a NavigationInstruction.Open with a NavigationKey that matched the provided predicate, but the NavigationKey did not match the predicate"
        }
    return instruction
}

/**
 * This method asserts that the last navigation instruction the NavigationHandle executed was a NavigationInstruction.Open
 * with a NavigationKey of the provided type [T] and direction (if the direction parameter was not null).
 *
 * If you want to assert that any NavigationInstruction.Open was executed, and don't care whether the instruction was the
 * last instruction or not, use [assertAnyInstructionOpened].
 */
inline fun <reified T : NavigationKey> TestNavigationHandle<*>.assertInstructionOpened(
    direction: NavigationDirection? = null
): NavigationInstruction.Open<*> {
    return assertInstructionOpened(T::class.java, direction)
}

/**
 * This method asserts that the NavigationHandle has executed a NavigationInstruction.Open with a NavigationKey of the
 * provided type [T] and direction (if the direction parameter was not null). This method does not care about the order
 * of the instructions executed by the NavigationHandle.
 *
 * If you care about ordering, and you want to assert on the last NavigationInstruction.Open executed, use [assertInstructionOpened].
 */
fun <T : NavigationKey> TestNavigationHandle<*>.assertAnyInstructionOpened(
    type: Class<T>,
    direction: NavigationDirection? = null,
    predicate: (T) -> Boolean = { true }
): NavigationInstruction.Open<*> {
    val openInstructions = instructions.filterIsInstance<NavigationInstruction.Open<*>>()

    if (openInstructions.isEmpty()) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open")
    }
    val instruction = openInstructions.lastOrNull {
        type.isAssignableFrom(it.navigationKey::class.java) &&
                runCatching { predicate(it.navigationKey as T) }.getOrDefault(false)
    }
    if (instruction == null) {
        enroAssertionError("NavigationHandle has not executed any NavigationInstruction.Open with a NavigationKey of type $type")
    }
    type.isAssignableFrom(instruction.navigationKey::class.java).shouldBeEqualTo(true) {
        "NavigationHandle was expected to have executed a NavigationInstruction.Open with a NavigationKey of type $type, but the NavigationKey was of type ${instruction.navigationKey::class.java}"
    }
    if (direction != null) {
        instruction.navigationDirection.shouldBeEqualTo(direction) {
            "NavigationHandle was expected to have executed a NavigationInstruction.Open with a NavigationDirection of $direction, but the NavigationDirection was ${instruction.navigationDirection}"
        }
    }
    return instruction
}

/**
 * This method asserts that the NavigationHandle has executed a NavigationInstruction.Open with a NavigationKey of the
 * provided type [T] and direction (if the direction parameter was not null). This method does not care about the order
 * of the instructions executed by the NavigationHandle.
 *
 * If you care about ordering, and you want to assert on the last NavigationInstruction.Open executed, use [assertInstructionOpened].
 */
inline fun <reified T : NavigationKey> TestNavigationHandle<*>.assertAnyInstructionOpened(
    direction: NavigationDirection? = null,
    noinline predicate: (T) -> Boolean = { true }
): NavigationInstruction.Open<*> {
    return assertAnyInstructionOpened(
        type = T::class.java,
        direction = direction,
        predicate = predicate
    )
}