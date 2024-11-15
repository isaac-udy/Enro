package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.NavigationContainerContext

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext is equal to the provided
 * NavigationInstruction [instruction]
 */
fun NavigationContainerContext.assertActive(
    instruction: NavigationInstruction.Open<*>,
) {
    backstack.active
        .shouldBeEqualTo(instruction) {
            "Active NavigationInstruction does not match expected value.\n\tExpected: $expected\n\tActual: $actual"
        }
}

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext matches the provided predicate
 *
 * @return The active NavigationInstruction that matches the predicate
 */
fun NavigationContainerContext.assertActive(
    predicate: (NavigationInstruction.Open<*>) -> Boolean,
): NavigationInstruction.Open<*> {
    backstack.active
        .shouldMatchPredicateNotNull(predicate) {
            "Active NavigationInstruction does not match predicate.\n\tWas: $actual"
        }
        .let { return it }
}

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext has a NavigationKey that is equal to
 * the provided NavigationKey [key]
 */
fun NavigationContainerContext.assertActive(
    key: NavigationKey,
) {
    backstack.active?.navigationKey.shouldBeEqualTo(key) {
        "Active NavigationInstruction's NavigationKey does not match expected value.\n\tExpected: $expected\n\tActual: $actual"
    }
}

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext has a NavigationKey that matches the
 * provided type T and the provided predicate
 *
 * @return The active NavigationInstruction's NavigationKey that matches the predicate
 */
inline fun <reified T : NavigationKey> NavigationContainerContext.assertActive(
    noinline predicate: (T) -> Boolean = { true }
) : T {
    backstack.active?.navigationKey
        .shouldBeInstanceOf<T>()
        .shouldMatchPredicateNotNull(predicate) {
            "Active NavigationInstruction's NavigationKey does not match predicate.\n\tWas: $actual"
        }
        .let { return it }
}

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext is not equal to the
 * provided NavigationInstruction [instruction]
 */
fun NavigationContainerContext.assertNotActive(
    instruction: NavigationInstruction.Open<*>
) {
    backstack.active.shouldNotBeEqualTo(instruction) {
        "Active NavigationInstruction should not be active.\n\tActive: $expected"
    }
}

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext does not match the provided predicate
 */
fun NavigationContainerContext.assertInstructionNotActive(
    predicate: (NavigationInstruction.Open<*>) -> Boolean
) {
    backstack.active.shouldNotBeEqualTo(predicate) {
        "Active NavigationInstruction should not be active.\n\tActive: $expected"
    }
}

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext has a NavigationKey that is not equal
 * to the provided NavigationKey [key]
 */
fun NavigationContainerContext.assertNotActive(
    key: NavigationKey
) {
    backstack.active?.navigationKey.shouldNotBeEqualTo(key) {
        "Active NavigationInstruction's NavigationKey should not be active.\n\tActive: $expected"
    }
}

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext has a NavigationKey that does not match the
 * provided type T and the provided predicate
 */
inline fun <reified T : NavigationKey> NavigationContainerContext.assertNotActive(
    noinline predicate: (T) -> Boolean = { true }
) {
    val activeKey = backstack.active?.navigationKey
    if (activeKey !is T) return
    activeKey.shouldNotMatchPredicate(predicate) {
        "Active NavigationInstruction's NavigationKey should not match predicate.\n\tWas: $actual"
    }
}