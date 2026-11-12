package dev.enro

import kotlin.jvm.JvmName

/**
 * Decides whether a [NavigationContainer] accepts a [NavigationKey.Instance].
 */
public class NavigationContainerFilter internal constructor(
    internal val fromChildrenOnly: Boolean = false,
    private val block: (NavigationKey.Instance<NavigationKey>) -> Boolean
) {
    public fun accepts(instance: NavigationKey.Instance<NavigationKey>): Boolean {
        return block(instance)
    }
}

/**
 * Builds a [NavigationContainerFilter] that matches a [NavigationKey.Instance] when at least
 * one registered rule matches. A builder with no rules matches no [NavigationKey.Instance].
 */
public class NavigationContainerFilterBuilder internal constructor() {
    private val filters: MutableList<NavigationContainerFilter> = mutableListOf()

    /**
     * Matches a [NavigationKey.Instance] whose key satisfies [predicate].
     */
    public fun key(predicate: (NavigationKey) -> Boolean) {
        filters.add(NavigationContainerFilter { predicate(it.key) })
    }

    /**
     * Matches a [NavigationKey.Instance] whose key is equal to [key].
     */
    public fun key(key: NavigationKey) {
        key { it == key }
    }

    /**
     * Matches a [NavigationKey.Instance] whose key is a [T] and satisfies [predicate].
     */
    @JvmName("keyWithType")
    public inline fun <reified T: NavigationKey> key(
        crossinline predicate: (T) -> Boolean = { true }
    ) {
        key { it is T && predicate(it) }
    }

    /**
     * Matches a [NavigationKey.Instance] that satisfies [predicate].
     */
    public fun instance(predicate: (NavigationKey.Instance<NavigationKey>) -> Boolean) {
        filters.add(
            NavigationContainerFilter(fromChildrenOnly = false, block = predicate)
        )
    }

    internal fun build(): NavigationContainerFilter {
        return NavigationContainerFilter(
            fromChildrenOnly = false,
        ) { instruction ->
            filters.any { it.accepts(instruction) }
        }
    }
}

/**
 * Returns a [NavigationContainerFilter] that accepts the same [NavigationKey.Instance]s as this
 * filter, but only for an operation executed by a destination inside the container.
 */
public fun NavigationContainerFilter.fromChildrenOnly(): NavigationContainerFilter {
    val filter = this
    return NavigationContainerFilter(fromChildrenOnly = true) { filter.accepts(it) }
}

/**
 * A [NavigationContainerFilter] that accepts all [NavigationKey.Instance].
 */
public fun acceptAll(): NavigationContainerFilter = NavigationContainerFilter { true }

/**
 * A [NavigationContainerFilter] that accepts no [NavigationKey.Instance]. Use [acceptNone] for
 * a container whose backstack is changed only through [NavigationContainer.updateBackstack].
 */
public fun acceptNone(): NavigationContainerFilter = NavigationContainerFilter { false }

/**
 * A [NavigationContainerFilter] that accepts each [NavigationKey.Instance] matched by a rule
 * registered in [block].
 */
public fun accept(block: NavigationContainerFilterBuilder.() -> Unit): NavigationContainerFilter {
    return NavigationContainerFilterBuilder()
        .apply(block)
        .build()
}


/**
 * A [NavigationContainerFilter] that accepts each [NavigationKey.Instance] that no rule
 * registered in [block] matches.
 */
public fun doNotAccept(block: NavigationContainerFilterBuilder.() -> Unit): NavigationContainerFilter {
    return NavigationContainerFilterBuilder()
        .apply(block)
        .build()
        .let { filter ->
            NavigationContainerFilter { !filter.accepts(it) }
        }
}