package dev.enro.destination.synthetic

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey

// Class-based overload for Java compatibility
public fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: Class<T>,
    provider: SyntheticDestinationProvider<T>,
): NavigationBinding<T, SyntheticDestination<*>> =
    createSyntheticNavigationBinding(
        navigationKeyType = navigationKeyType.kotlin,
        provider = provider
    )

// Class-based overload for Java compatibility
public fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: Class<T>,
    destination: () -> SyntheticDestination<T>
): NavigationBinding<T, SyntheticDestination<*>> =
    createSyntheticNavigationBinding(
        navigationKeyType = navigationKeyType.kotlin,
        destination = destination
    )
