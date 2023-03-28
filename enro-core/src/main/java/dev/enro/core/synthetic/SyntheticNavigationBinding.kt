package dev.enro.core.synthetic

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import kotlin.reflect.KClass


public class SyntheticNavigationBinding<KeyType : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    internal val destination: () -> SyntheticDestination<KeyType>
) : NavigationBinding<KeyType, SyntheticDestination<*>> {
    override val destinationType: KClass<SyntheticDestination<*>> = SyntheticDestination::class
    override val baseType: KClass<in SyntheticDestination<*>> = SyntheticDestination::class
}

public fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: Class<T>,
    destination: () -> SyntheticDestination<T>
): NavigationBinding<T, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = navigationKeyType.kotlin,
        destination = destination
    )

public inline fun <reified KeyType : NavigationKey> createSyntheticNavigationBinding(
    noinline destination: () -> SyntheticDestination<KeyType>
): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        destination = destination
    )

public fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: Class<T>,
    provider: SyntheticDestinationProvider<T>,
): NavigationBinding<T, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = navigationKeyType.kotlin,
        destination = provider::create
    )

public inline fun <reified KeyType : NavigationKey> createSyntheticNavigationBinding(
    provider: SyntheticDestinationProvider<KeyType>,
): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        destination = provider::create
    )


public inline fun <reified KeyType : NavigationKey, reified DestinationType : SyntheticDestination<KeyType>> createSyntheticNavigationBinding(): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        destination = { DestinationType::class.java.newInstance() }
    )

public inline fun <reified KeyType : NavigationKey, reified DestinationType : SyntheticDestination<KeyType>> NavigationModuleScope.syntheticDestination() {
    binding(createSyntheticNavigationBinding<KeyType, DestinationType>())
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(noinline destination: () -> SyntheticDestination<KeyType>) {
    binding(createSyntheticNavigationBinding(destination))
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(noinline block: SyntheticDestinationScope<KeyType>.() -> Unit) {
    val provider: SyntheticDestinationProvider<KeyType> = dev.enro.core.synthetic.syntheticDestination(block)
    binding(createSyntheticNavigationBinding(provider))
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(provider: SyntheticDestinationProvider<KeyType>) {
    binding(createSyntheticNavigationBinding(provider))
}
