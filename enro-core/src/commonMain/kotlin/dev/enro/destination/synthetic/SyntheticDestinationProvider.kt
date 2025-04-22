package dev.enro.destination.synthetic

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey

public class SyntheticDestinationScope<T : NavigationKey> internal constructor(
    internal val destination: SyntheticDestination<T>,
) {
    public val navigationContext: NavigationContext<out Any> = destination.navigationContext
    public val key: T = destination.key
    public val instruction: AnyOpenInstruction = destination.instruction
}

public class SyntheticDestinationProvider<T : NavigationKey> internal constructor(
    private val block: SyntheticDestinationScope<T>.() -> Unit
) {
    @PublishedApi
    internal fun create() : SyntheticDestination<T> {
        return object : SyntheticDestination<T>() {
            override fun process() {
                SyntheticDestinationScope(this)
                    .block()
            }
        }
    }
}

public fun <T : NavigationKey> syntheticDestination(block: SyntheticDestinationScope<T>.() -> Unit): SyntheticDestinationProvider<T> {
    return SyntheticDestinationProvider(block)
}

public fun <T : NavigationKey> syntheticDestinationProvider(block: SyntheticDestinationScope<T>.() -> Unit): SyntheticDestinationProvider<T> {
    return SyntheticDestinationProvider(block)
}