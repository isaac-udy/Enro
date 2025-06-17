package dev.enro.core.synthetic

import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.destinations.SyntheticDestinationScope

public fun <T : NavigationKey> syntheticDestination(
    block: SyntheticDestinationScope<T>.() -> Unit
): NavigationDestinationProvider<T> {
    return dev.enro.ui.destinations.syntheticDestination({  }, block)
}