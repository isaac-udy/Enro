@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation

fun TestNavigationHandle<out NavigationKey>.assertCompleted() {
    val operation = operations.lastOrNull()
    enroAssert(operation != null) {
        "Expected the last operation to be a complete operation, but there were no operations."
    }
    enroAssert(operation is NavigationOperation.Complete<*>) {
        "Expected the last operation to be a complete operation, but it was ${operation::class.simpleName}"
    }
    enroAssert(operation.instance.id == instance.id) {
        "Expected the last operation to be a complete operation for this NavigationHandle's instance, but it was for ${operation.instance.id}"
    }
}

inline fun <reified R : Any> TestNavigationHandle<out NavigationKey.WithResult<R>>.assertCompleted(expected: R) {
    assertCompleted { it == expected }
}

inline fun <reified T : Any> TestNavigationHandle<out NavigationKey.WithResult<T>>.assertCompleted(
    predicate: (T) -> Boolean = { true },
): T {
    val operation = operations.lastOrNull()
    enroAssert(operation != null) {
        "Expected the last operation to be a complete operation, but there were no operations."
    }
    enroAssert(operation is NavigationOperation.Complete<*>) {
        "Expected the last operation to be a complete operation, but it was ${operation::class.simpleName}"
    }
    enroAssert(operation.instance.id == instance.id) {
        "Expected the last operation to be a complete operation for this NavigationHandle's instance, but it was for ${operation.instance.id}"
    }
    val result = operation.result
    enroAssert(result != null) {
        "Expected the last operation to be a complete operation with a result, but it contained a null result"
    }
    enroAssert(result is T) {
        "Expected the last operation to be a complete operation with a result of type ${T::class.simpleName}, but it was ${result::class.simpleName}"
    }
    enroAssert(predicate(result)) {
        "Expected the last operation to be a complete operation with a result that matches the predicate, but it did not"
    }
    return result
}

fun TestNavigationHandle<*>.assertNotCompleted() {
    val last = operations.lastOrNull()
    if (last !is NavigationOperation.Complete<*>) return
    require(last.instance.id != instance.id) {
        "Expected the last operation to not be a complete operation for instance ${instance.id}"
    }
}
