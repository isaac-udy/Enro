package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation

/**
 * Asserts that the [TestNavigationHandle]'s operation history contains at least one operation of type [T]
 * matching the provided [predicate].
 *
 * @return The last matching operation.
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
 * Asserts that the [TestNavigationHandle]'s operation history does not contain an [Open] operation
 * of type [T] matching the provided [predicate].
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

