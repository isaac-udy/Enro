package dev.enro.destination.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.defaultSerializer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

public class ComposableNavigationBinding<KeyType : NavigationKey, ComposableType : ComposableDestination> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ComposableType>,
    internal val constructDestination: () -> ComposableType,
    override val keySerializer: KSerializer<KeyType>,
) : NavigationBinding<KeyType, ComposableType>() {
    override val baseType: KClass<in ComposableType> = ComposableDestination::class
}

@PublishedApi
internal fun <KeyType : NavigationKey> createComposableNavigationBinding(
    keyType: KClass<KeyType>,
    keySerializer: KSerializer<KeyType>,
    content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    class Destination : ComposableDestination() {
        @Composable
        override fun Render() {
            content()
        }
    }
    return ComposableNavigationBinding(
        keyType = keyType,
        destinationType = Destination()::class as KClass<ComposableDestination>,
        keySerializer = keySerializer,
        constructDestination = { Destination() }
    )
}

@PublishedApi
internal fun <KeyType : NavigationKey, DestinationType: ComposableDestination> createComposableNavigationBinding(
    keyType: KClass<KeyType>,
    destinationType: KClass<DestinationType>,
    keySerializer: KSerializer<KeyType>,
    constructDestination: () -> DestinationType,
): NavigationBinding<KeyType, DestinationType> {
    return ComposableNavigationBinding(
        keyType = keyType,
        destinationType = destinationType,
        keySerializer = keySerializer,
        constructDestination = constructDestination,
    )
}

public inline fun <reified KeyType : NavigationKey> createComposableNavigationBinding(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return createComposableNavigationBinding(
        keyType = KeyType::class,
        keySerializer = keySerializer,
        content = content
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> createComposableNavigationBinding(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline constructDestination: () -> DestinationType,
): NavigationBinding<KeyType, DestinationType> {
    return createComposableNavigationBinding(
        keyType = KeyType::class,
        destinationType = DestinationType::class,
        keySerializer = keySerializer,
        constructDestination = constructDestination,
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> NavigationModuleScope.composableDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline constructDestination: () -> DestinationType,
) {
    binding(
        createComposableNavigationBinding<KeyType, DestinationType>(
            keySerializer = keySerializer,
            constructDestination = constructDestination,
        )
    )
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.composableDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline content: @Composable () -> Unit,
) {
    binding(
        createComposableNavigationBinding<KeyType>(
            keySerializer = keySerializer,
            content = content,
        )
    )
}
