package dev.enro.destination.synthetic

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.default
import kotlin.reflect.KClass

public class SyntheticNavigationBinding<KeyType : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    internal val destination: () -> SyntheticDestination<KeyType>,
    override val keySerializer: NavigationKeySerializer<KeyType> = NavigationKeySerializer.default(keyType),
) : NavigationBinding<KeyType, SyntheticDestination<*>> {
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
    destination: () -> SyntheticDestination<T>
): NavigationBinding<T, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = navigationKeyType,
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
    navigationKeyType: KClass<T>,
    provider: SyntheticDestinationProvider<T>,
): NavigationBinding<T, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = navigationKeyType,
        destination = provider::create
    )

public inline fun <reified KeyType : NavigationKey> createSyntheticNavigationBinding(
    provider: SyntheticDestinationProvider<KeyType>,
): NavigationBinding<KeyType, SyntheticDestination<*>> =
    SyntheticNavigationBinding(
        keyType = KeyType::class,
        destination = provider::create
    )

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(noinline destination: () -> SyntheticDestination<KeyType>) {
    binding(createSyntheticNavigationBinding(destination))
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(noinline block: SyntheticDestinationScope<KeyType>.() -> Unit) {
    val provider: SyntheticDestinationProvider<KeyType> = syntheticDestinationProvider(block)
    binding(createSyntheticNavigationBinding(provider))
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.syntheticDestination(provider: SyntheticDestinationProvider<KeyType>) {
    binding(createSyntheticNavigationBinding(provider))
}