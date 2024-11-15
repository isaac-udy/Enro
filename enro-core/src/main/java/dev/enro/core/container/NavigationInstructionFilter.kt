package dev.enro.core.container

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.result.flows.FlowStep

/**
 * A NavigationContainerFilter is used to determine whether or not a given [NavigationInstruction.Open]
 * should be accepted by a [NavigationContainer] to be handled/displayed by that container.
 */
public class NavigationInstructionFilter internal constructor(
    public val accept: (NavigationInstruction.Open<*>) -> Boolean
)

/**
 * A builder for creating a [NavigationInstructionFilter]
 */
public class NavigationContainerFilterBuilder internal constructor() {
    private val filters: MutableList<NavigationInstructionFilter> = mutableListOf()

    /**
     * Matches any instructions that have a NavigationKey that returns true for the provided predicate
     */
    public fun key(predicate: (NavigationKey) -> Boolean) {
        filters.add(NavigationInstructionFilter { predicate(it.navigationKey) })
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
    public fun instruction(predicate: (NavigationInstruction.Open<*>) -> Boolean) {
        filters.add(NavigationInstructionFilter(predicate))
    }

    /**
     * Matches any instructions that are presented (i.e. navigationDirection is NavigationDirection.Present)
     */
    public fun anyPresented() {
        instruction { it.navigationDirection == NavigationDirection.Present }
    }

    /**
     * Matches any instructions that are pushed (i.e. navigationDirection is NavigationDirection.Pushed)
     */
    public fun anyPushed() {
        instruction { it.navigationDirection == NavigationDirection.Push }
    }

    internal fun build(): NavigationInstructionFilter {
        return NavigationInstructionFilter { instruction ->
            filters.any { it.accept(instruction) }
        }
    }
}

/**
 * A [NavigationInstructionFilter] that accepts all [NavigationInstruction.Open] instructions.
 */
public fun acceptAll(): NavigationInstructionFilter = NavigationInstructionFilter { true }

/**
 * A [NavigationInstructionFilter] that accepts only [NavigationInstruction.Open] instructions which have been added to the container
 * by a [dev.enro.core.result.flows.NavigationFlow].
 */
public fun acceptFromFlow(): NavigationInstructionFilter = NavigationInstructionFilter {
    it.internal.resultKey is FlowStep<*>
}

/**
 * A [NavigationInstructionFilter] that accepts no [NavigationInstruction.Open] instructions.
 *
 * This is useful in cases where a Navigation Container should only contain the initial destination,
 * or where the Navigation Container only has it's backstack updated manually through the
 * [NavigationContainer.setBackstack] method
 */
public fun acceptNone(): NavigationInstructionFilter = NavigationInstructionFilter { false }

/**
 * A [NavigationInstructionFilter] that accepts [NavigationInstruction.Open] instructions
 * that match configuration provided a NavigationContainerFilterBuilder created using the [block].
 */
public fun accept(block: NavigationContainerFilterBuilder.() -> Unit): NavigationInstructionFilter {
    return NavigationContainerFilterBuilder()
        .apply(block)
        .build()
}

/**
 * A [NavigationInstructionFilter] that accepts [NaviationKey]s that return true the provided function.
 * This method is provided for backwards compatibility, and it should be preferred to use the
 * [accept] method instead, as this provides a more readable way of creating filters for NavigationKeys.
 *
 * For example:
 * ```
 *     accept {
 *        key<ExampleKey>()
 *        key<AnotherKey>()
 *        key<ThirdKey>()
 *     }
 * ```
 * is more readable and easily maintainable than the equivalent:
 * ```
 *     acceptKey { it is ExampleKey || it is AnotherKey || it is ThirdKey }
 * ```
 */
@Deprecated("Prefer accept { key { ... } } instead")
public fun acceptKey(block: (NavigationKey) -> Boolean): NavigationInstructionFilter {
    return accept {
        key(block)
    }
}


/**
 * A [NavigationInstructionFilter] that accepts [NavigationInstruction.Open] instructions
 * that do not match configuration provided a NavigationContainerFilterBuilder created using the [block].
 */
public fun doNotAccept(block: NavigationContainerFilterBuilder.() -> Unit): NavigationInstructionFilter {
    return NavigationContainerFilterBuilder()
        .apply(block)
        .build()
        .let { filter ->
            NavigationInstructionFilter { !filter.accept(it) }
        }
}