package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass

public class ComposableNavigationBinding<KeyType : NavigationKey, ComposableType : ComposableDestination> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ComposableType>,
    internal val constructDestination: () -> ComposableType = { destinationType.java.newInstance() }
) : NavigationBinding<KeyType, ComposableType> {
    override val baseType: KClass<in ComposableType> = ComposableDestination::class
}

public fun <KeyType : NavigationKey, ComposableType : ComposableDestination> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    composableType: Class<ComposableType>
): NavigationBinding<KeyType, ComposableType> {
    return ComposableNavigationBinding(
        keyType = keyType.kotlin,
        destinationType = composableType.kotlin
    )
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


public inline fun <reified KeyType : NavigationKey> createComposableNavigationBinding(
    noinline content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return createComposableNavigationBinding(
        KeyType::class,
        content
    )
}


public fun <KeyType : NavigationKey> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    val destination = object : ComposableDestination() {
        @Composable
        override fun Render() {
            content()
        }
    }
    return ComposableNavigationBinding(
        keyType = keyType.kotlin,
        destinationType = destination::class
    ) as NavigationBinding<KeyType, ComposableDestination>
}

public inline fun <reified KeyType : NavigationKey, reified ComposableType : ComposableDestination> createComposableNavigationBinding(): NavigationBinding<KeyType, ComposableType> {
    return createComposableNavigationBinding(
        KeyType::class.java,
        ComposableType::class.java
    )
}