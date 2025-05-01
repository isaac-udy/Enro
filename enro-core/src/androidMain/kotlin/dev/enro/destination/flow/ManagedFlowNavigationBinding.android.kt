package dev.enro.destination.flow

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlinx.serialization.KSerializer

// Overload for Java class interop
public fun <T : NavigationKey, Result> createManagedFlowNavigationBinding(
    navigationKeyType: Class<T>,
    serializer: KSerializer<T>,
    provider: ManagedFlowDestinationProvider<T, Result>,
): NavigationBinding<T, ManagedFlowDestination<*, *>> =
    ManagedFlowNavigationBinding(
        keyType = navigationKeyType.kotlin,
        keySerializer = serializer,
        destination = provider::create
    )
