@file:Suppress("PackageDirectoryMismatch")
package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.destination.compose.ComposableDestination
import dev.enro.destination.compose.createComposableNavigationBinding as newCreateComposableNavigationBinding
import dev.enro.destination.compose.composableDestination as newComposableDestination

@Deprecated("Use dev.enro.destination.compose.ComposableDestination instead", ReplaceWith("dev.enro.destination.compose.ComposableDestination"))
public typealias ComposableDestination = dev.enro.destination.compose.ComposableDestination

@Deprecated("Use dev.enro.destination.compose.createComposableNavigationBinding instead", ReplaceWith("dev.enro.destination.compose.createComposableNavigationBinding(content)", "dev.enro.destination.compose.createComposableNavigationBinding"))
public inline fun <reified KeyType : NavigationKey> createComposableNavigationBinding(
    noinline content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return newCreateComposableNavigationBinding(content)
}

@Deprecated("Use dev.enro.destination.compose.createComposableNavigationBinding instead", ReplaceWith("dev.enro.destination.compose.createComposableNavigationBinding()", "dev.enro.destination.compose.createComposableNavigationBinding"))
public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> createComposableNavigationBinding(): NavigationBinding<KeyType, DestinationType> {
    val constructor = DestinationType::class.java.constructors.first { it.parameters.isEmpty() }
    return newCreateComposableNavigationBinding<KeyType, DestinationType> { constructor.newInstance() as DestinationType }
}

@Deprecated("Use dev.enro.destination.compose.composableDestination instead", ReplaceWith("dev.enro.destination.compose.composableDestination()", "dev.enro.destination.compose.composableDestination"))
public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> NavigationModuleScope.composableDestination() {
    val constructor = DestinationType::class.java.constructors.first { it.parameters.isEmpty() }
    newComposableDestination<KeyType, DestinationType> { constructor.newInstance() as DestinationType }
}

@Deprecated("Use dev.enro.destination.compose.composableDestination instead", ReplaceWith("dev.enro.destination.compose.composableDestination(content)", "dev.enro.destination.compose.composableDestination"))
public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.composableDestination(noinline content: @Composable () -> Unit) {
    newComposableDestination<KeyType>(content)
}