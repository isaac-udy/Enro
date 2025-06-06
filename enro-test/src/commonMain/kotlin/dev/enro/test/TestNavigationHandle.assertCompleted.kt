@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResult.Completed.Companion.result
import dev.enro.result.getResult

fun TestNavigationHandle<NavigationKey>.assertCompleted() {
    val result = instance.getResult()
    if (result !is NavigationResult.Completed) {
        enroAssertionError("NavigationHandle was expected to be completed")
    }
}

inline fun <reified R : Any> TestNavigationHandle<NavigationKey.WithResult<R>>.assertCompleted(expected: R) {
    assertCompleted { it == expected }
}

inline fun <reified T : Any> TestNavigationHandle<NavigationKey.WithResult<T>>.assertCompleted(
    predicate: (T) -> Boolean = { true },
): T {
    val result = instance.getResult()
    if (result !is NavigationResult.Completed) {
        enroAssertionError("NavigationHandle was expected to be completed")
    }
    @Suppress("UNCHECKED_CAST")
    result as NavigationResult.Completed<out T>
    if (result.data !is T) {
        enroAssertionError("NavigationHandle was expected to have result of type")
    }
    val typedResult = result.result

    if (!predicate(typedResult)) {
        enroAssertionError("NavigationHandle result did not match the provided predicate")
    }
    return typedResult
}

fun TestNavigationHandle<*>.assertNotCompleted() {
    val result = instance.getResult()
    if (result is NavigationResult.Completed) {
        enroAssertionError("NavigationHandle was expected to not be completed")
    }
}
