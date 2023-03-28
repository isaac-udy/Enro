package dev.enro.core.synthetic

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey

public class SyntheticDestinationScope<T : NavigationKey> internal constructor(
    public val navigationContext: NavigationContext<out Any>,
    public val key: T,
    public val instruction: AnyOpenInstruction,
)

public class SyntheticDestinationProvider<T : NavigationKey> internal constructor(
    private val block: SyntheticDestinationScope<T>.() -> Unit
) {
    @PublishedApi
    internal fun create() : SyntheticDestination<T> {
        return object : SyntheticDestination<T>() {
            override fun process() {
                SyntheticDestinationScope(navigationContext, key, instruction)
                    .block()
            }
        }
    }
}

public fun <T : NavigationKey> syntheticDestination(block: SyntheticDestinationScope<T>.() -> Unit): SyntheticDestinationProvider<T> {
    return SyntheticDestinationProvider(block)
}