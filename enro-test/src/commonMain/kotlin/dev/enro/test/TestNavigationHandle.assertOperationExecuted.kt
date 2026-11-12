package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation

/**
 * Asserts that the recorded operations of the [TestNavigationHandle] contain at least one
 * operation of type [T] that matches [predicate]. Returns the last matching operation.
 */
public inline fun <reified T : NavigationOperation.RootOperation> TestNavigationHandle<*>.assertOperationExecuted(
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
 * Asserts that the recorded operations of the [TestNavigationHandle] contain no Open
 * operation whose instance matches [predicate].
 */
public inline fun <reified T : NavigationKey> TestNavigationHandle<*>.assertOperationNotExecuted(
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

