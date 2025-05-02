package dev.enro.destination.synthetic

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.serialization.defaultSerializer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

public class SyntheticNavigationBinding<KeyType : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val keySerializer: KSerializer<KeyType>,
    internal val destination: () -> SyntheticDestination<KeyType>,
) : NavigationBinding<KeyType, SyntheticDestination<*>>() {
    override val destinationType: KClass<SyntheticDestination<*>> = SyntheticDestination::class
    override val baseType: KClass<in SyntheticDestination<*>> = SyntheticDestination::class

    public fun execute(
        fromContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ) {
        require(keyType == instruction.navigationKey::class)
        val instance = destination.invoke()
        instance.bind(
            navigationContext = fromContext,
            instruction = instruction,
        )
        instance.process()
    }
}

public fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: KClass<T>,
    keySerializer: KSerializer<T>,
    destination: () -> SyntheticDestination<T>,
): NavigationBinding<T, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = navigationKeyType,
        destination = destination,
        keySerializer = keySerializer,
    )

public inline fun <reified KeyType : NavigationKey> createSyntheticNavigationBinding(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline destination: () -> SyntheticDestination<KeyType>
): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        keySerializer = keySerializer,
        destination = destination,
    )

public fun <T : NavigationKey> createSyntheticNavigationBinding(
    navigationKeyType: KClass<T>,
    keySerializer: KSerializer<T>,
    provider: SyntheticDestinationProvider<T>,
): NavigationBinding<T, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = navigationKeyType,
        keySerializer = keySerializer,
        destination = provider::create,
    )

public inline fun <reified KeyType : NavigationKey> createSyntheticNavigationBinding(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    provider: SyntheticDestinationProvider<KeyType>,
): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        keySerializer = keySerializer,
        destination = provider::create,
    )

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline destination: () -> SyntheticDestination<KeyType>
) {
    binding(
        createSyntheticNavigationBinding(
            keySerializer = keySerializer,
            destination = destination,
        )
    )
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline block: SyntheticDestinationScope<KeyType>.() -> Unit,
) {
    val provider: SyntheticDestinationProvider<KeyType> = syntheticDestinationProvider(block)
    binding(
        createSyntheticNavigationBinding(
            keySerializer = keySerializer,
            provider = provider,
        )
    )
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    provider: SyntheticDestinationProvider<KeyType>,
) {
    binding(
        createSyntheticNavigationBinding(
            keySerializer = keySerializer,
            provider = provider,
        )
    )
}