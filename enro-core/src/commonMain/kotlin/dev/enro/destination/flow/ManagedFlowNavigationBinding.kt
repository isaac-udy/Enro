package dev.enro.destination.flow

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.default
import kotlin.reflect.KClass

public class ManagedFlowNavigationBinding<KeyType : NavigationKey, Result> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    internal val destination: (TypedNavigationHandle<KeyType>) -> ManagedFlowDestination<KeyType, Result>,
    override val keySerializer: NavigationKeySerializer<KeyType> = NavigationKeySerializer.default(keyType),
) : NavigationBinding<KeyType, ManagedFlowDestination<*, *>>() {
    override val destinationType: KClass<ManagedFlowDestination<*, *>> = ManagedFlowDestination::class
    override val baseType: KClass<in ManagedFlowDestination<*, *>> = ManagedFlowDestination::class
}

public fun <T : NavigationKey, Result> createManagedFlowNavigationBinding(
    navigationKeyType: KClass<T>,
    provider: ManagedFlowDestinationProvider<T, Result>,
): NavigationBinding<T, ManagedFlowDestination<*, *>> =
    ManagedFlowNavigationBinding(
        keyType = navigationKeyType,
        destination = provider::create
    )

public inline fun <reified KeyType : NavigationKey, Result> createManagedFlowNavigationBinding(
    provider: ManagedFlowDestinationProvider<KeyType, Result>,
): NavigationBinding<KeyType, ManagedFlowDestination<*, *>> =
    ManagedFlowNavigationBinding(
        keyType = KeyType::class,
        destination = provider::create
    )

public inline fun <reified KeyType : NavigationKey, Result> NavigationModuleScope.managedFlowDestination(
    provider: ManagedFlowDestinationProvider<KeyType, Result>,
) {
    binding(createManagedFlowNavigationBinding(provider))
}
