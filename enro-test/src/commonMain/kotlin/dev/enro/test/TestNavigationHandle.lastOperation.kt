package dev.enro.test

import dev.enro.NavigationOperation
import kotlin.reflect.KClass

/**
 * Returns the most-recently executed operation. Throws if no operations have
 * been executed yet — use `operations.lastOrNull()` directly if you need
 * to handle the empty case.
 */
public fun TestNavigationHandle<*>.lastOperation(): NavigationOperation.RootOperation {
    val last = operations.lastOrNull()
    last.shouldNotBeEqualTo(null) {
        "TestNavigationHandle should have executed at least one operation, but none were executed"
    }
    return last!!
}

/**
 * Returns the most-recently executed operation of type [T], or throws if no
 * operation of that type has been executed. Useful when you want to drill
 * into operation-specific fields (instance, result, etc.) without filtering
 * the whole `operations` list manually.
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
 * Asserts the executed operations match [expectedSequence] exactly in order
 * and count. Compares operation types by `KClass`, not by content — for
 * field-level assertions, use [assertOperationExecuted] per step.
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
