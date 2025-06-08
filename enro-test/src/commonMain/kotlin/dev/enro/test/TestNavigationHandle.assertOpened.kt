package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation

inline fun <reified T : NavigationKey> TestNavigationHandle<*>.assertOpened(): NavigationKey.Instance<T> {
    return assertOperationExecuted<NavigationOperation.Open<T>>().instance
}

inline fun <reified T : NavigationKey> TestNavigationHandle<*>.assertOpened(
    instance: NavigationKey.Instance<T>,
): NavigationKey.Instance<T> {
    return assertOpened<T> { it == instance }
}

inline fun <reified T : NavigationKey> TestNavigationHandle<*>.assertOpened(
    predicate: (NavigationKey.Instance<T>) -> Boolean = { true },
): NavigationKey.Instance<T> {
    return assertOperationExecuted<NavigationOperation.Open<T>> {
        predicate(it.instance)
    }.instance
}

inline fun <reified T : NavigationKey> TestNavigationHandle<*>.assertOpened(
    key: T,
): NavigationKey.Instance<T> {
    return assertOpened<T> { it.key == key }
}