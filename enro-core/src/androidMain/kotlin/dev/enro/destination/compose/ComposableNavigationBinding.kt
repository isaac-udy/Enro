package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.default
import kotlin.reflect.KClass

public class ComposableNavigationBinding<KeyType : NavigationKey, ComposableType : ComposableDestination> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ComposableType>,
    internal val constructDestination: () -> ComposableType,
    override val keySerializer: NavigationKeySerializer<KeyType> = NavigationKeySerializer.default(keyType),
) : NavigationBinding<KeyType, ComposableType> {
    override val baseType: KClass<in ComposableType> = ComposableDestination::class
}

@PublishedApi
internal fun <KeyType : NavigationKey> createComposableNavigationBinding(
    keyType: KClass<KeyType>,
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
        constructDestination = { Destination() }
    )
}

@PublishedApi
internal fun <KeyType : NavigationKey, DestinationType: ComposableDestination> createComposableNavigationBinding(
    keyType: KClass<KeyType>,
    destinationType: KClass<DestinationType>,
): NavigationBinding<KeyType, DestinationType> {
    val constructor = destinationType.constructors.first { it.parameters.isEmpty() }
    return ComposableNavigationBinding(
        keyType = keyType,
        destinationType = destinationType,
        constructDestination = { constructor.call() }
    )
}

public inline fun <reified KeyType : NavigationKey> createComposableNavigationBinding(
    noinline content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return createComposableNavigationBinding(
        KeyType::class,
        content
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> createComposableNavigationBinding(): NavigationBinding<KeyType, DestinationType> {
    return createComposableNavigationBinding(
        keyType = KeyType::class,
        destinationType = DestinationType::class,
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> NavigationModuleScope.composableDestination() {
    binding(createComposableNavigationBinding<KeyType, DestinationType>())
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.composableDestination(noinline content: @Composable () -> Unit) {
    binding(createComposableNavigationBinding<KeyType>(content))
}

// Class-based overload for Java compatibility
public fun <KeyType : NavigationKey> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return createComposableNavigationBinding(
        keyType = keyType.kotlin,
        content = content
    )
}

// Class-based overload for Java compatibility
public fun <KeyType : NavigationKey, ComposableType : ComposableDestination> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    composableType: Class<ComposableType>
): NavigationBinding<KeyType, ComposableType> {
    return createComposableNavigationBinding(
        keyType = keyType.kotlin,
        destinationType = composableType.kotlin
    )
}