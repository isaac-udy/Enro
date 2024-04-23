package dev.enro.core

import dev.enro.core.container.NavigationContainer
import kotlin.reflect.KClass

/**
 * Finds a NavigationContext that matches the predicate. This will search the entire hierarchy of NavigationContexts starting
 * at the context it is invoked on (not the root). All child contexts are considered, including contexts which are not in the
 * active NavigationContainer (e.g. if there are two containers visible, both the active and non-active container will be searched).
 *
 * If you want to search the entire hierarchy, including the root, you should call this function on the root NavigationContext,
 * which can be accessed from any NavigationContext by using the [rootContext] function.
 */
public fun NavigationContext<*>.findContext(predicate: (NavigationContext<*>) -> Boolean): NavigationContext<*>? {
    val contexts = mutableListOf(this)
    while (contexts.isNotEmpty()) {
        val context = contexts.removeAt(0)
        if (predicate(context)) {
            return context
        }
        val children = context.containerManager.containers.flatMap {
            setOfNotNull(
                it.getChildContext(NavigationContainer.ContextFilter.ActivePushed),
                it.getChildContext(NavigationContainer.ContextFilter.ActivePresented),
            )
        }
        contexts.addAll(children)
    }
    return null
}

/**
 * Requires a NavigationContext that matches the predicate. A wrapper for [findContext] that throws an exception if
 * no matching context is found.
 *
 * @see [findContext]
 */
public fun NavigationContext<*>.requireContext(predicate: (NavigationContext<*>) -> Boolean): NavigationContext<*> {
    return requireNotNull(findContext(predicate))
}

/**
 * Finds a NavigationContext that has a NavigationKey of type [keyType].
 *
 * @see [findContext]
 */
public fun NavigationContext<*>.findContextWithKey(keyType: KClass<*>): NavigationContext<*>? {
    return findContext {
        val key = it.instruction?.navigationKey ?: return@findContext false
        key::class == keyType
    }
}

/**
 * Requires a NavigationContext that has a NavigationKey of type [keyType].
 *
 * @see [findContext]
 */
public fun NavigationContext<*>.requireContextWithKey(keyType: KClass<*>): NavigationContext<*> {
    return requireContext {
        val key = it.instruction?.navigationKey ?: return@requireContext false
        key::class == keyType
    }
}

/**
 * Finds a NavigationContext that has a NavigationKey of type [T].
 *
 * @see [findContext]
 */
public inline fun <reified T> NavigationContext<*>.findContextWithKey(): NavigationContext<*>? {
    return findContext { it.instruction?.navigationKey is T }
}

/**
 * Requires a NavigationContext that has a NavigationKey of type [T].
 *
 * @see [findContext]
 */
public inline fun <reified T> NavigationContext<*>.requireContextWithKey(): NavigationContext<*> {
    return requireContext { it.instruction?.navigationKey is T }
}

/**
 * Finds a NavigationContext that has a NavigationKey of matching [predicate].
 *
 * @see [findContext]
 */
public inline fun <reified T> NavigationContext<*>.findContextWithKey(crossinline predicate: (NavigationKey) -> Boolean): NavigationContext<*>? {
    return findContext { it.instruction?.navigationKey?.let(predicate) ?: false }
}

/**
 * Requires a NavigationContext that has a NavigationKey of matching [predicate].
 *
 * @see [findContext]
 */
public inline fun <reified T> NavigationContext<*>.requireContextWithKey(crossinline predicate: (NavigationKey) -> Boolean): NavigationContext<*> {
    return requireContext { it.instruction?.navigationKey?.let(predicate) ?: false }
}
