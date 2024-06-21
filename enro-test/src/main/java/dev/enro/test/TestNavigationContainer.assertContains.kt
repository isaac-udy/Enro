package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.NavigationContainerContext

/**
 * Asserts that the NavigationContainerContext's backstack contains at least one NavigationInstruction that is equal
 * to the provided NavigationInstruction [instruction]
 */
fun NavigationContainerContext.assertContains(
    instruction: NavigationInstruction.Open<*>
) {
    backstack.firstOrNull { it == instruction }
        .shouldBeEqualTo(
            instruction,
        ) {
            "NavigationContainer's backstack does not contain expected NavigationInstruction.\n\tExpected NavigationInstruction: $expected\n\tBackstack: $backstack"
        }
}

/**
 * Asserts that the NavigationContainerContext's backstack contains at least one NavigationInstruction that has a
 * NavigationKey that is equal to the provided NavigationKey [key]
 */
fun NavigationContainerContext.assertContains(
    key: NavigationKey
) {
    val backstackAsNavigationKeys = backstack.map { it.navigationKey }
    backstackAsNavigationKeys
        .firstOrNull { it == key }
        .shouldBeEqualTo(
            key,
        ) {
            "NavigationContainer's backstack does not contain expected NavigationKey.\n\tExpected NavigationKey: $expected\n\tBackstack: $backstackAsNavigationKeys"
        }
}

/**
 * Asserts that the NavigationContainerContext's backstack does not contain a NavigationInstruction that is equal to
 * the provided NavigationInstruction [instruction]
 */
fun NavigationContainerContext.assertDoesNotContain(
    instruction: NavigationInstruction.Open<*>
) {
    backstack.firstOrNull { it == instruction }
        .shouldNotBeEqualTo(
            instruction,
        ) {
            "NavigationContainer's backstack should not contain NavigationInstruction.\n\tNavigationInstruction: $expected\n\tBackstack: $backstack"
        }
}

/**
 * Asserts that the NavigationContainerContext's backstack does not contain an instruction that has a NavigationKey that is
 * equal to the provided NavigationKey [key]
 */
fun NavigationContainerContext.assertDoesNotContain(
    key: NavigationKey
) {
    val backstackAsNavigationKeys = backstack.map { it.navigationKey }
    backstack.firstOrNull { it == key }
        .shouldNotBeEqualTo(
            key,
        ) {
            "NavigationContainer's backstack should not contain NavigationKey.\n\tNavigationInstruction: $expected\n\tBackstack: $backstackAsNavigationKeys"
        }
}