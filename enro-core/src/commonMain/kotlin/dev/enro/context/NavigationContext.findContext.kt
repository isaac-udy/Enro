package dev.enro.context

import dev.enro.NavigationContainer
import dev.enro.NavigationKey


private fun AnyNavigationContext.findContext(
    activeOnly: Boolean,
    predicate: (AnyNavigationContext) -> Boolean,
): AnyNavigationContext? {
    if (predicate(this)) return this
    children.onEach { child ->
        child as AnyNavigationContext

        if (activeOnly && !child.isActive) return@onEach
        child.findContext(
            activeOnly = activeOnly,
            predicate = predicate
        )?.let { return it }
    }
    return null
}

public fun AnyNavigationContext.findContext(
    predicate: (AnyNavigationContext) -> Boolean,
): AnyNavigationContext? {
    return findContext(activeOnly = false, predicate = predicate)
}

public fun AnyNavigationContext.findActiveContext(
    predicate: (AnyNavigationContext) -> Boolean,
): AnyNavigationContext? {
    return findContext(activeOnly = true, predicate = predicate)
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T: NavigationKey> AnyNavigationContext.findContext(
    crossinline predicate: (DestinationContext<T>) -> Boolean = { true },
): DestinationContext<T>? {
    return findContext {
        it is DestinationContext<*> && it.key is T && predicate(it as DestinationContext<T>)
    } as? DestinationContext<T>
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T: NavigationKey> AnyNavigationContext.findActiveContext(
    crossinline predicate: (DestinationContext<T>) -> Boolean = { true },
): DestinationContext<T>? {
    return findActiveContext {
        @Suppress("UNCHECKED_CAST")
        it is DestinationContext<*> && it.key is T && predicate(it as DestinationContext<T>)
    } as? DestinationContext<T>
}

public inline fun <reified T : NavigationKey> AnyNavigationContext.findContext(
    key: T,
): AnyNavigationContext? = findContext<T> {
    it.key == key
}

public inline fun <reified T : NavigationKey> AnyNavigationContext.findActiveContext(
    key: T,
): AnyNavigationContext? = findActiveContext<T> {
    it.key == key
}

public fun AnyNavigationContext.findContext(
    key: NavigationContainer.Key,
): AnyNavigationContext? {
    return findContext {
        it is ContainerContext && it.container.key == key
    }
}

public fun AnyNavigationContext.findActiveContext(
    key: NavigationContainer.Key,
): AnyNavigationContext? {
    return findActiveContext {
        it is ContainerContext && it.container.key == key
    }
}
