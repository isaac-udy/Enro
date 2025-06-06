package dev.enro.context

import dev.enro.NavigationContainer
import dev.enro.NavigationKey

public fun AnyNavigationContext.requireContext(
    predicate: (AnyNavigationContext) -> Boolean,
): AnyNavigationContext {
    val found = findContext(predicate = predicate)
    return requireNotNull(found) {
        "Could not find a context that matches the given predicate from NavigationContext with id: $id"
    }
}

public fun AnyNavigationContext.requireActiveContext(
    predicate: (AnyNavigationContext) -> Boolean,
): AnyNavigationContext {
    val found = findActiveContext(predicate = predicate)
    return requireNotNull(found) {
        "Could not find a context that matches the given predicate from NavigationContext with id: $id"
    }
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T: NavigationKey> AnyNavigationContext.requireContext(
    crossinline predicate: (DestinationContext<T>) -> Boolean = { true },
): DestinationContext<T> {
    return requireContext {
        it is DestinationContext<*> && it.key is T && predicate(it as DestinationContext<T>)
    } as DestinationContext<T>
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T: NavigationKey> AnyNavigationContext.requireActiveContext(
    crossinline predicate: (DestinationContext<T>) -> Boolean = { true },
): DestinationContext<T> {
    return requireActiveContext {
        @Suppress("UNCHECKED_CAST")
        it is DestinationContext<*> && it.key is T && predicate(it as DestinationContext<T>)
    } as DestinationContext<T>
}

public inline fun <reified T : NavigationKey> AnyNavigationContext.requireContext(
    key: T,
): AnyNavigationContext? = requireContext<T> {
    it.key == key
}

public inline fun <reified T : NavigationKey> AnyNavigationContext.requireActiveContext(
    key: T,
): AnyNavigationContext = requireActiveContext<T> {
    it.key == key
}

public fun AnyNavigationContext.requireContext(
    key: NavigationContainer.Key,
): AnyNavigationContext {
    return requireContext {
        it is ContainerContext && it.container.key == key
    }
}

public fun AnyNavigationContext.requireActiveContext(
    key: NavigationContainer.Key,
): AnyNavigationContext {
    return requireActiveContext {
        it is ContainerContext && it.container.key == key
    }
}
