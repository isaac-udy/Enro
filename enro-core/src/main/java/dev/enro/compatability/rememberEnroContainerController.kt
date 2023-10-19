@file:Suppress("PackageDirectoryMismatch")

package dev.enro.core.compose

import androidx.compose.runtime.Composable
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder

@Composable
@Deprecated("Use the rememberEnroContainerController that takes a List<NavigationKey> instead of a List<NavigationInstruction.Open>")
public fun rememberEnroContainerController(
    initialBackstack: List<AnyOpenInstruction> = emptyList(),
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    interceptor: NavigationInterceptorBuilder.() -> Unit = {},
    animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    accept: (NavigationKey) -> Boolean = { true },
): ComposableNavigationContainer {
    return rememberNavigationContainer(
        initialBackstack = initialBackstack.toBackstack(),
        emptyBehavior = emptyBehavior,
        interceptor = interceptor,
        animations = animations,
        accept = accept,
    )
}
