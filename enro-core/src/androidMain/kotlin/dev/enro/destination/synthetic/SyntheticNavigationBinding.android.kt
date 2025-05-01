package dev.enro.destination.synthetic

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlinx.serialization.KSerializer

// Class-based overload for Java compatibility
public fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: Class<T>,
    keySerializer: KSerializer<T>,
    provider: SyntheticDestinationProvider<T>,
): NavigationBinding<T, SyntheticDestination<*>> =
    createSyntheticNavigationBinding(
        navigationKeyType = navigationKeyType.kotlin,
        keySerializer = keySerializer,
        provider = provider,
    )

// Class-based overload for Java compatibility
public fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: Class<T>,
    keySerializer: KSerializer<T>,
    destination: () -> SyntheticDestination<T>,
): NavigationBinding<T, SyntheticDestination<*>> =
    createSyntheticNavigationBinding(
        navigationKeyType = navigationKeyType.kotlin,
        keySerializer = keySerializer,
        destination = destination
    )
