package dev.enro.destination.flow

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey

// Overload for Java class interop
public fun <T : NavigationKey, Result> createManagedFlowNavigationBinding(
    navigationKeyType: Class<T>,
    provider: ManagedFlowDestinationProvider<T, Result>,
): NavigationBinding<T, ManagedFlowDestination<*, *>> =
    ManagedFlowNavigationBinding(
        keyType = navigationKeyType.kotlin,
        destination = provider::create
    )
