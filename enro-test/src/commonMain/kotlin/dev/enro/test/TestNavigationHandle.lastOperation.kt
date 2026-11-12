package dev.enro.test

import dev.enro.NavigationOperation
import kotlin.reflect.KClass

/**
 * Returns the last executed operation. Throws when no operation has been executed.
 */
public fun TestNavigationHandle<*>.lastOperation(): NavigationOperation.RootOperation {
    val last = operations.lastOrNull()
    last.shouldNotBeEqualTo(null) {
        "TestNavigationHandle should have executed at least one operation, but none were executed"
    }
    return last!!
}

/**
 * Returns the last executed operation of type [T]. Throws when no operation of type [T]
 * has been executed.
 */
public inline fun <reified T : NavigationOperation.RootOperation> TestNavigationHandle<*>.lastOperationOfType(): T {
    val matching = operations.filterIsInstance<T>()
    matching.lastOrNull().shouldNotBeEqualTo(null) {
        "TestNavigationHandle should have executed at least one ${T::class.simpleName}, " +
            "but none were found.\n\tOperations: $operations"
    }
    return matching.last()
}

/**
 * Asserts that the types of the executed operations equal [expectedSequence], in order and
 * in count. The assertion compares the `KClass` of each operation, not the content of the
 * operation; use [assertOperationExecuted] to assert on the content of an operation.
 */
public fun TestNavigationHandle<*>.assertOperationSequence(
    vararg expectedSequence: KClass<out NavigationOperation.RootOperation>,
) {
    val actualTypes = operations.map { it::class }
    val expectedTypes = expectedSequence.toList()
    enroAssert(actualTypes == expectedTypes) {
        "Expected operation sequence ${expectedTypes.map { it.simpleName }}, " +
            "but executed ${actualTypes.map { it.simpleName }}.\n\tOperations: $operations"
    }
}
