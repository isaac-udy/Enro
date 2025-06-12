package dev.enro.destination.synthetic

import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.destinations.SyntheticDestinationScope


public fun <T : NavigationKey> syntheticDestination(
    metadata: Map<String, Any> = emptyMap(),
    block: SyntheticDestinationScope<T>.() -> Unit
): NavigationDestinationProvider<T> {
    return dev.enro.ui.destinations.syntheticDestination(metadata, block)
}