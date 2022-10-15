package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass

class ComposableNavigationBinding<KeyType : NavigationKey, ComposableType : ComposableDestination> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ComposableType>
) : NavigationBinding<KeyType, ComposableType>

fun <KeyType : NavigationKey, ComposableType : ComposableDestination> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    composableType: Class<ComposableType>
): NavigationBinding<KeyType, ComposableType> = ComposableNavigationBinding(
    keyType = keyType.kotlin,
    destinationType = composableType.kotlin
)

inline fun <reified KeyType : NavigationKey> createComposableNavigationBinding(
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
        destinationType = destination::class
    ) as NavigationBinding<KeyType, ComposableDestination>
}


fun <KeyType : NavigationKey> createComposableNavigationBinding(
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

inline fun <reified KeyType : NavigationKey, reified ComposableType : ComposableDestination> createComposableNavigationBinding() =
    createComposableNavigationBinding(
        KeyType::class.java,
        ComposableType::class.java
    )