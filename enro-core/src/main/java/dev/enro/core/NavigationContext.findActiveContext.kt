package dev.enro.core

import dev.enro.core.container.NavigationContainer
import kotlin.reflect.KClass


/**
 * Finds a NavigationContext that matches the predicate. This will search the hierarchy of active NavigationContexts starting
 * at the context it is invoked on (not the root). Only active child contexts are considered (e.g. if there are
 * two containers visible, only the active container will be searched).
 *
 * If you want to search the entire hierarchy, including the root, you should call this function on the root NavigationContext,
 * which can be accessed from any NavigationContext by using the [rootContext] function.
 */
public fun NavigationContext<*>.findActiveContext(predicate: (NavigationContext<*>) -> Boolean): NavigationContext<*>? {
    val contexts = mutableListOf(this)
    while (contexts.isNotEmpty()) {
        val context = contexts.removeAt(0)
        if (predicate(context)) {
            return context
        }
        val children = context.containerManager.activeContainer?.let {
            setOfNotNull(
                it.getChildContext(NavigationContainer.ContextFilter.ActivePushed),
                it.getChildContext(NavigationContainer.ContextFilter.ActivePresented),
            )
        }.orEmpty()
        contexts.addAll(children)
    }
    return null
}

/**
 * Requires an active NavigationContext that matches the predicate. A wrapper for [findActiveContext] that throws an exception if
 * no matching context is found.
 *
 * @see [findActiveContext]
 */
public fun NavigationContext<*>.requireActiveContext(predicate: (NavigationContext<*>) -> Boolean): NavigationContext<*> {
    return requireNotNull(findActiveContext(predicate))
}

/**
 * Finds an active NavigationContext that has a NavigationKey of type [keyType].
 *
 * @see [findActiveContext]
 */
public fun NavigationContext<*>.findActiveContextWithKey(keyType: KClass<*>): NavigationContext<*>? {
    return findActiveContext {
        val key = it.instruction?.navigationKey ?: return@findActiveContext false
        key::class == keyType
    }
}

/**
 * Requires an active NavigationContext that has a NavigationKey of type [keyType].
 *
 * @see [findActiveContext]
 */
public fun NavigationContext<*>.requireActiveContextWithKey(keyType: KClass<*>): NavigationContext<*> {
    return requireContext {
        val key = it.instruction?.navigationKey ?: return@requireContext false
        key::class == keyType
    }
}

/**
 * Finds an active NavigationContext that has a NavigationKey of type [T].
 *
 * @see [findActiveContext]
 */
public inline fun <reified T> NavigationContext<*>.findActiveContextWithKey(): NavigationContext<*>? {
    return findActiveContext { it.instruction?.navigationKey is T }
}

/**
 * Requires an active NavigationContext that has a NavigationKey of type [T].
 *
 * @see [findActiveContext]
 */
public inline fun <reified T> NavigationContext<*>.requireActiveContextWithKey(): NavigationContext<*> {
    return requireContext { it.instruction?.navigationKey is T }
}

/**
 * Finds an active NavigationContext that has a NavigationKey of matching [predicate].
 *
 * @see [findActiveContext]
 */
public inline fun <reified T> NavigationContext<*>.findActiveContextWithKey(crossinline predicate: (NavigationKey) -> Boolean): NavigationContext<*>? {
    return findActiveContext { it.instruction?.navigationKey?.let(predicate) ?: false }
}

/**
 * Requires an active NavigationContext that has a NavigationKey of matching [predicate].
 *
 * @see [findActiveContext]
 */
public inline fun <reified T> NavigationContext<*>.requireActiveContextWithKey(crossinline predicate: (NavigationKey) -> Boolean): NavigationContext<*> {
    return requireContext { it.instruction?.navigationKey?.let(predicate) ?: false }
}
