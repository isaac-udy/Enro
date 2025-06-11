package dev.enro

import kotlin.jvm.JvmName

/**
 * A NavigationContainerFilter is used to determine whether or not a given [NavigationKey.Instance] should be accepted by a [NavigationContainer] to be handled/displayed by that container.
 */
public class NavigationContainerFilter internal constructor(
    internal val fromChildrenOnly: Boolean = false,
    private val block: (NavigationKey.Instance<NavigationKey>) -> Boolean
) {
    // validates
    public fun accepts(instance: NavigationKey.Instance<NavigationKey>): Boolean {
        return block(instance)
    }
}

/**
 * A builder for creating a [NavigationContainerFilter]
 */
public class NavigationContainerFilterBuilder internal constructor() {
    private val filters: MutableList<NavigationContainerFilter> = mutableListOf()

    /**
     * Matches any instructions that have a NavigationKey that returns true for the provided predicate
     */
    public fun key(predicate: (NavigationKey) -> Boolean) {
        filters.add(NavigationContainerFilter { predicate(it.key) })
    }

    /**
     * Matches any instructions that have a NavigationKey that is equal to the provided key
     */
    public fun key(key: NavigationKey) {
        key { it == key }
    }

    /**
     * Matches any instructions that match the provided predicate
     */
    @JvmName("keyWithType")
    public inline fun <reified T: NavigationKey> key(
        crossinline predicate: (T) -> Boolean = { true }
    ) {
        key { it is T && predicate(it) }
    }

    /**
     * Matches any instructions that match the provided predicate
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
 * A [NavigationContainerFilter] that accepts all [NavigationKey.Instance].
 */
public fun acceptAll(): NavigationContainerFilter = NavigationContainerFilter { true }

/**
 * A [NavigationContainerFilter] that accepts no [NavigationKey.Instance].
 *
 * This is useful in cases where a Navigation Container should only contain the initial destination,
 * or where the Navigation Container only has it's backstack updated manually through the
 * [NavigationContainer.setBackstack] method
 */
public fun acceptNone(): NavigationContainerFilter = NavigationContainerFilter { false }

/**
 * A [NavigationContainerFilter] that accepts [NavigationKey.Instance]
 * that match configuration provided a NavigationContainerFilterBuilder created using the [block].
 */
public fun accept(block: NavigationContainerFilterBuilder.() -> Unit): NavigationContainerFilter {
    return NavigationContainerFilterBuilder()
        .apply(block)
        .build()
}


/**
 * A [NavigationContainerFilter] that accepts [NavigationKey.Instance]
 * that do not match configuration provided a NavigationContainerFilterBuilder created using the [block].
 */
public fun doNotAccept(block: NavigationContainerFilterBuilder.() -> Unit): NavigationContainerFilter {
    return NavigationContainerFilterBuilder()
        .apply(block)
        .build()
        .let { filter ->
            NavigationContainerFilter { !filter.accepts(it) }
        }
}