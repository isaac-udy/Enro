package dev.enro.test

import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer

/**
 * Asserts that the NavigationContainer's backstack is equal to the provided NavigationBackstack [backstack]
 */
fun NavigationContainer.assertBackstackEquals(
    backstack: NavigationBackstack,
) {
    val actualBackstack = this.backstack
    val expectedBackstack = backstack

    actualBackstack.size.shouldBeEqualTo(expectedBackstack.size) {
        "NavigationContainer's backstack size was expected to be $expected, but was $actual\n\tExpected backstack: $expectedBackstack\n\tActual backstack: $actualBackstack"
    }
    expectedBackstack.zip(actualBackstack)
        .forEachIndexed { index, (expected, actual) ->
            expected.shouldBeEqualTo(actual) {
                "Index $index in NavigationContainer's backstack was expected to be $expected, but was $actual\n\tExpected backstack: $expectedBackstack\n\tActual backstack: $actualBackstack"
            }
        }
}

/**
 * Asserts that the NavigationContainer's backstack matches the provided predicate
 */
fun NavigationContainer.assertBackstackMatches(
    predicate: (NavigationBackstack) -> Boolean,
) {
    val actualBackstack = this.backstack

    actualBackstack.shouldMatchPredicateNotNull(predicate) {
        "NavigationContainer's backstack did not match predicate\n\tActual backstack: $actualBackstack"
    }
}
