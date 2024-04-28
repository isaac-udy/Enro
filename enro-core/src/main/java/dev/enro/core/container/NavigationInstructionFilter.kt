package dev.enro.core.container

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey

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

    public fun key(predicate: (NavigationKey) -> Boolean) {
        filters.add(NavigationInstructionFilter { predicate(it.navigationKey) })
    }

    public fun key(key: NavigationKey) {
        key { it == key }
    }

    public inline fun <reified T: NavigationKey> key() {
        key { it is T }
    }

    public fun instruction(predicate: (NavigationInstruction.Open<*>) -> Boolean) {
        filters.add(NavigationInstructionFilter(predicate))
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