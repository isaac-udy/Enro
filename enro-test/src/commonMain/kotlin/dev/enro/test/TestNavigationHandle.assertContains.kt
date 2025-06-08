package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation

/**
 * Asserts that the NavigationContainer's backstack contains at least one NavigationKey.Instance that matches the
 * provided predicate.
 *
 * @return The first NavigationKey.Instance that matches the predicate
 */
inline fun <reified T : NavigationOperation.RootOperation> TestNavigationHandle<*>.assertOperationExecuted(
    predicate: (T) -> Boolean = { true },
): T {
    operations
        .filterIsInstance<T>()
        .lastOrNull { predicate(it) }
        .shouldNotBeEqualTo(null) {
            "TestNavigationHandle should have executed an operation matching the predicate.\n\tOperations: $operations"
        }
        .let {
            return it!!
        }
}

/**
 * Asserts that the NavigationContainer's backstack does not contain a NavigationKey.Instance that matches the provided
 * predicate
 */
inline fun <reified T : NavigationKey> TestNavigationHandle<*>.assertOperationNotExecuted(
    predicate: (NavigationKey.Instance<T>) -> Boolean,
) {
    operations
        .filterIsInstance<NavigationOperation.Open<T>>()
        .lastOrNull {
            predicate(it.instance)
        }
        .shouldBeEqualTo(
            null,
        ) {
            "NavigationHandle should not have executed an operation matching the predicate.\n\tOperations: $operations"
        }
}

