@file:Suppress("PackageDirectoryMismatch")
package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.serialization.defaultSerializer
import dev.enro.destination.compose.ComposableDestination
import kotlinx.serialization.KSerializer
import dev.enro.destination.compose.composableDestination as newComposableDestination
import dev.enro.destination.compose.createComposableNavigationBinding as newCreateComposableNavigationBinding

@Deprecated("Use dev.enro.destination.compose.ComposableDestination instead", ReplaceWith("dev.enro.destination.compose.ComposableDestination"))
public typealias ComposableDestination = dev.enro.destination.compose.ComposableDestination

@Deprecated("Use dev.enro.destination.compose.createComposableNavigationBinding instead", ReplaceWith("dev.enro.destination.compose.createComposableNavigationBinding(content)", "dev.enro.destination.compose.createComposableNavigationBinding"))
public inline fun <reified KeyType : NavigationKey> createComposableNavigationBinding(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer<KeyType>(),
    noinline content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return newCreateComposableNavigationBinding(keySerializer, content)
}

@Deprecated("Use dev.enro.destination.compose.createComposableNavigationBinding instead", ReplaceWith("dev.enro.destination.compose.createComposableNavigationBinding()", "dev.enro.destination.compose.createComposableNavigationBinding"))
public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> createComposableNavigationBinding(): NavigationBinding<KeyType, DestinationType> {
    val constructor = DestinationType::class.constructors.first { it.parameters.isEmpty() }
    return newCreateComposableNavigationBinding<KeyType, DestinationType> { constructor.call() }
}

@Deprecated("Use dev.enro.destination.compose.composableDestination instead", ReplaceWith("dev.enro.destination.compose.composableDestination()", "dev.enro.destination.compose.composableDestination"))
public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> NavigationModuleScope.composableDestination() {
    val constructor = DestinationType::class.constructors.first { it.parameters.isEmpty() }
    newComposableDestination<KeyType, DestinationType> { constructor.call() }
}

@Deprecated("Use dev.enro.destination.compose.composableDestination instead", ReplaceWith("dev.enro.destination.compose.composableDestination(content)", "dev.enro.destination.compose.composableDestination"))
public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.composableDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline content: @Composable () -> Unit,
) {
    newComposableDestination<KeyType>(keySerializer, content)
}