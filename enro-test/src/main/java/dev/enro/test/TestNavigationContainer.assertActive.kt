package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.NavigationContainerContext

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext is equal to the provided
 * NavigationInstruction [instruction]
 */
fun NavigationContainerContext.assertActive(
    instruction: NavigationInstruction.Open<*>
) {
    backstack.active.shouldBeEqualTo(instruction) {
        "Active NavigationInstruction does not match expected value.\n\tExpected: $expected\n\tActual: $actual"
    }
}

/**
 * Asserts that the active NavigationInstruction in the NavigationContainerContext has a NavigationKey that is equal to
 * the provided NavigationKey [key]
 */
fun NavigationContainerContext.assertActive(
    key: NavigationKey
) {
    backstack.active?.navigationKey.shouldBeEqualTo(key) {
        "Active NavigationInstruction's NavigationKey does not match expected value.\n\tExpected: $expected\n\tActual: $actual"
    }
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