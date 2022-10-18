package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass

public class ComposableNavigationBinding<KeyType : NavigationKey, ComposableType : ComposableDestination> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ComposableType>,
    internal val constructDestination: () -> ComposableType = { destinationType.java.newInstance() }
) : NavigationBinding<KeyType, ComposableType>

public fun <KeyType : NavigationKey, ComposableType : ComposableDestination> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    composableType: Class<ComposableType>
): NavigationBinding<KeyType, ComposableType> {
    return ComposableNavigationBinding(
        keyType = keyType.kotlin,
        destinationType = composableType.kotlin
    )
}

public inline fun <reified KeyType : NavigationKey> createComposableNavigationBinding(
    crossinline content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    val destination = object : ComposableDestination() {
        @Composable
        override fun Render() {
            content()
        }
    }
    return ComposableNavigationBinding(
        keyType = KeyType::class,
        destinationType = destination::class as KClass<ComposableDestination>,
        constructDestination = { destination }
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