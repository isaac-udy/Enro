package dev.enro.core.synthetic

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass


class SyntheticNavigationBinding<KeyType : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    val destination: () -> SyntheticDestination<KeyType>
) : NavigationBinding<KeyType, SyntheticDestination<*>> {
    override val destinationType: KClass<SyntheticDestination<*>> = SyntheticDestination::class
}

fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: Class<T>,
    destination: () -> SyntheticDestination<T>
): NavigationBinding<T, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = navigationKeyType.kotlin,
        destination = destination
    )

inline fun <reified KeyType : NavigationKey> createSyntheticNavigationBinding(
    noinline destination: () -> SyntheticDestination<KeyType>
): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        destination = destination
    )

inline fun <reified KeyType : NavigationKey, reified DestinationType : SyntheticDestination<KeyType>> createSyntheticNavigationBinding(): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        destination = { DestinationType::class.java.newInstance() }
    )