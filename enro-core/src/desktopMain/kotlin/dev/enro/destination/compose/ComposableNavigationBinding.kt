package dev.enro.destination.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.default
import kotlin.reflect.KClass

/**
 * Desktop implementation of ComposableNavigationBinding
 */
public actual class ComposableNavigationBinding<KeyType : NavigationKey, ComposableType : ComposableDestination> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ComposableType>,
    internal val constructDestination: () -> ComposableType,
    override val keySerializer: NavigationKeySerializer<KeyType> = NavigationKeySerializer.default(keyType),
) : NavigationBinding<KeyType, ComposableType> {
    override val baseType: KClass<in ComposableType> = ComposableDestination::class
}

/**
 * Creates a composable navigation binding with a content block
 */
@PublishedApi
internal actual fun <KeyType : NavigationKey> createComposableNavigationBinding(
    keyType: KClass<KeyType>,
    content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    class Destination : ComposableDestination() {
        @Composable
        override fun Render() {
            content()
        }
    }
    @Suppress("UNCHECKED_CAST")
    return ComposableNavigationBinding(
        keyType = keyType,
        destinationType = Destination::class as KClass<ComposableDestination>,
        constructDestination = { Destination() }
    )
}

/**
 * Creates a composable navigation binding with a destination type
 */
@PublishedApi
internal actual fun <KeyType : NavigationKey, DestinationType : ComposableDestination> createComposableNavigationBinding(
    keyType: KClass<KeyType>,
    destinationType: KClass<DestinationType>
): NavigationBinding<KeyType, DestinationType> {
    val constructor = destinationType.constructors.first { it.parameters.isEmpty() }
    return ComposableNavigationBinding(
        keyType = keyType,
        destinationType = destinationType,
        constructDestination = { constructor.call() }
    )
}