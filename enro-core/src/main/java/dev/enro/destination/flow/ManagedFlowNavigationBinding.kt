package dev.enro.destination.flow

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import kotlin.reflect.KClass

public class ManagedFlowNavigationBinding<KeyType : NavigationKey, Result> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    internal val destination: (TypedNavigationHandle<KeyType>) -> ManagedFlowDestination<KeyType, Result>
) : NavigationBinding<KeyType, ManagedFlowDestination<*, *>> {
    override val destinationType: KClass<ManagedFlowDestination<*, *>> = ManagedFlowDestination::class
    override val baseType: KClass<in ManagedFlowDestination<*, *>> = ManagedFlowDestination::class
}

public fun <T : NavigationKey, Result> createManagedFlowNavigationBinding(
    navigationKeyType: Class<T>,
    provider: ManagedFlowDestinationProvider<T, Result>,
): NavigationBinding<T, ManagedFlowDestination<*, *>> =
    ManagedFlowNavigationBinding(
        keyType = navigationKeyType.kotlin,
        destination = provider::create
    )

public inline fun <reified KeyType : NavigationKey, Result> createManagedFlowNavigationBinding(
    provider: ManagedFlowDestinationProvider<KeyType, Result>,
): NavigationBinding<KeyType, ManagedFlowDestination<*, *>> =
    ManagedFlowNavigationBinding(
        keyType = KeyType::class,
        destination = provider::create
    )