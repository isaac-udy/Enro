package dev.enro.test

import dev.enro.core.NavigationInstruction
import kotlin.reflect.KClass

/**
 * Asserts that the NavigationHandle has executed a Close.WithResult instruction, and that the result matches the provided predicate
 *
 * @return the result of the Close.WithResult instruction
 */
fun <T : Any> TestNavigationHandle<*>.assertClosedWithResult(
    type: KClass<T>,
    predicate: (T) -> Boolean = { true },
) : T {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close.WithResult>()
        .lastOrNull()

    instruction.shouldNotBeEqualTo(null) {
        "NavigationHandle was expected to have executed a Close.WithResult instruction, but no Close.WithResult instruction was found"
    }
    requireNotNull(instruction)

    val result = instruction.result
    val isAssignable = type.isInstance(result)
    isAssignable.shouldBeEqualTo(true) {
        "NavigationHandle's Close.WithResult was expected to be assignable to ${type}, but was of type ${instruction.result::class}"
    }
    @Suppress("UNCHECKED_CAST")
    result as T

    predicate(result).shouldBeEqualTo(true) {
        "NavigationHandle's Close.WithResult did not match the provided predicate\n\tResult: $result"
    }
    return result
}

/**
 * Asserts that the NavigationHandle has executed a Close.WithResult instruction, and that the result matches the provided predicate
 *
 * @return the result of the Close.WithResult instruction
 */
inline fun <reified T : Any> TestNavigationHandle<*>.assertClosedWithResult(
    predicate: (T) -> Boolean = { true },
) : T {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close.WithResult>()
        .lastOrNull()

    instruction.shouldNotBeEqualTo(null) {
        "NavigationHandle was expected to have executed a Close.WithResult instruction, but no Close.WithResult instruction was found"
    }
    requireNotNull(instruction)

    val result = instruction.result
    val isAssignable = T::class.isInstance(result)
    isAssignable.shouldBeEqualTo(true) {
        "NavigationHandle's Close.WithResult was expected to be assignable to ${T::class}, but was of type ${instruction.result::class}"
    }
    @Suppress("UNCHECKED_CAST")
    result as T

    predicate(result).shouldBeEqualTo(true) {
        "NavigationHandle's Close.WithResult did not match the provided predicate\n\tResult: $result"
    }
    return result
}

/**
 * Asserts that the NavigationHandle has executed a Close.WithResult instruction, and that the result is equal to [expected]
 */
fun <T : Any> TestNavigationHandle<*>.assertClosedWithResult(
    expected: T,
) {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close.WithResult>()
        .lastOrNull()

    instruction.shouldNotBeEqualTo(null) {
        "NavigationHandle was expected to have executed a Close.WithResult instruction, but no Close.WithResult instruction was found"
    }
    requireNotNull(instruction)

    val result = instruction.result
    result.shouldBeEqualTo(expected) {
        "NavigationHandle's Close.WithResult was expected to be $expected, but was $result"
    }
}

/**
 * Asserts that the NavigationHandle has not executed a Close.WithResult instruction
 */
@Deprecated("Use assertNotClosed or assertClosedWithNoResult")
fun TestNavigationHandle<*>.assertNotClosedWithResult() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close.WithResult>()
        .lastOrNull()

    instruction.shouldBeEqualTo(null) {
        "NavigationHandle should not have executed a Close.WithResult instruction, but a Close.WithResult instruction was found"
    }
}

/**
 * Asserts that the NavigationHandle has executed a Close instruction, but not a Close.WithResult instruction
 */
fun TestNavigationHandle<*>.assertClosedWithNoResult() {
    val closeInstruction = assertClosed()
    if (closeInstruction is NavigationInstruction.Close.WithResult) {
        enroAssertionError("NavigationHandle was closed with result:\n\t${closeInstruction.result}")
    }
}