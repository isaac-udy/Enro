@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import kotlin.reflect.KClass

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

fun <R : Any> TestNavigationHandle<NavigationKey>.assertCompleted(
    type: KClass<R>,
    expected: R,
): R = assertCompleted(type) { it == expected }

inline fun <reified R : Any> TestNavigationHandle<NavigationKey>.assertCompleted(
    expected: R
): R = assertCompleted(R::class) { it == expected }

inline fun <reified T : Any> TestNavigationHandle<NavigationKey>.assertCompleted(
    noinline predicate: (T) -> Boolean = { true },
): T = assertCompleted(T::class, predicate)

fun <T : Any> TestNavigationHandle<NavigationKey>.assertCompleted(
    type: KClass<T>,
    predicate: (T) -> Boolean = { true },
): T {
    val operation = operations.lastOrNull()
    enroAssert(key is NavigationKey.WithResult<*>) {
        "Expected TestNavigationHandle's NavigationKey to be a NavigationKey.WithResult, but was ${key::class.simpleName}"
    }
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
    enroAssert(type.isInstance(result)) {
        "Expected the last operation to be a complete operation with a result of type ${type::class.simpleName}, but it was ${result::class.simpleName}"
    }
    @Suppress("UNCHECKED_CAST")
    result as T
    enroAssert(predicate(result)) {
        "Expected the last operation to be a complete operation with a result that matches the predicate, but it did not"
    }
    return result
}

fun TestNavigationHandle<*>.assertNotCompleted() {
    val last = operations.lastOrNull()
    if (last !is NavigationOperation.Complete<*>) return
    enroAssert(last.instance.id != instance.id) {
        "Expected the last operation to not be a complete operation for instance ${instance.id}"
    }
}
