package dev.enro.destination.flow

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.serialization.defaultSerializer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

public class ManagedFlowNavigationBinding<KeyType : NavigationKey, Result> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    internal val destination: (TypedNavigationHandle<KeyType>) -> ManagedFlowDestination<KeyType, Result>,
    override val keySerializer: KSerializer<KeyType>,
) : NavigationBinding<KeyType, ManagedFlowDestination<*, *>>() {
    override val destinationType: KClass<ManagedFlowDestination<*, *>> = ManagedFlowDestination::class
    override val baseType: KClass<in ManagedFlowDestination<*, *>> = ManagedFlowDestination::class
}

public fun <T : NavigationKey, Result> createManagedFlowNavigationBinding(
    navigationKeyType: KClass<T>,
    keySerializer: KSerializer<T>,
    provider: ManagedFlowDestinationProvider<T, Result>,
): NavigationBinding<T, ManagedFlowDestination<*, *>> =
    ManagedFlowNavigationBinding(
        keyType = navigationKeyType,
        destination = provider::create,
        keySerializer = keySerializer,
    )

public inline fun <reified KeyType : NavigationKey, Result> createManagedFlowNavigationBinding(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    provider: ManagedFlowDestinationProvider<KeyType, Result>,
): NavigationBinding<KeyType, ManagedFlowDestination<*, *>> =
    ManagedFlowNavigationBinding(
        keyType = KeyType::class,
        keySerializer = keySerializer,
        destination = provider::create,
    )

public inline fun <reified KeyType : NavigationKey, Result> NavigationModuleScope.managedFlowDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    provider: ManagedFlowDestinationProvider<KeyType, Result>,
) {
    binding(
        createManagedFlowNavigationBinding(
            keySerializer = keySerializer,
            provider = provider,
        )
    )
}
