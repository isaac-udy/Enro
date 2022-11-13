package dev.enro.synthetic

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.SyntheticDestination
import dev.enro.core.controller.NavigationComponentBuilder
import kotlin.reflect.KClass


public class SyntheticNavigationBinding<KeyType : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    internal val destination: () -> SyntheticDestination<KeyType>
) : NavigationBinding<KeyType, SyntheticDestination<*>> {
    override val destinationType: KClass<SyntheticDestination<*>> = SyntheticDestination::class
    override val baseDestinationType: KClass<SyntheticDestination<*>> = SyntheticDestination::class
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

public inline fun <reified KeyType : NavigationKey, reified DestinationType : SyntheticDestination<KeyType>> createSyntheticNavigationBinding(): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        destination = { DestinationType::class.java.newInstance() }
    )


public inline fun <reified KeyType : NavigationKey, reified DestinationType : SyntheticDestination<KeyType>> NavigationComponentBuilder.syntheticDestination() {
    binding(createSyntheticNavigationBinding<KeyType, DestinationType>())
}

public inline fun <reified KeyType : NavigationKey> NavigationComponentBuilder.syntheticDestination(
    noinline destination: () -> SyntheticDestination<KeyType>
) {
    binding(createSyntheticNavigationBinding(destination))
}