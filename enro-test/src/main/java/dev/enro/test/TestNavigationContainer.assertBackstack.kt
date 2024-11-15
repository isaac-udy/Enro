package dev.enro.test

import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainerContext

/**
 * Asserts that the NavigationContainerContext's backstack is equal to the provided NavigationBackstack [backstack]
 */
fun NavigationContainerContext.assertBackstackEquals(
    backstack: NavigationBackstack
) {
    val actualBackstack = this.backstack
    val expectedBackstack = backstack

    actualBackstack.size.shouldBeEqualTo(expectedBackstack) {
        "NavigationContainer's backstack size was expected to be $expected, but was $actual\n\tExpected backstack: $expectedBackstack\n\tActual backstack: $actualBackstack"
    }
    backstack.zip(actualBackstack)
        .forEachIndexed { index, (expected, actual) ->
            expected.shouldBeEqualTo(actual) {
                "Index $index in NavigationContainer's backstack was expected to be $expected, but was $actual\n\tExpected backstack: $backstack\n\tActual backstack: $actualBackstack"
            }
        }
}

/**
 * Asserts that the NavigationContainerContext's backstack matches the provided predicate
 */
fun NavigationContainerContext.assertBackstackMatches(
    predicate: (NavigationBackstack) -> Boolean
) {
    val actualBackstack = this.backstack

    actualBackstack.shouldMatchPredicateNotNull(predicate) {
        "NavigationContainer's backstack did not match predicate\n\tActual backstack: $actualBackstack"
    }
}