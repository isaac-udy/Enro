package dev.enro.destination.ios.hosts

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.destination.compose.ComposableNavigationBinding
import dev.enro.destination.ios.EnroUIViewController
import dev.enro.destination.ios.uiWindowDestination
import kotlinx.serialization.Serializable
import platform.UIKit.UIWindow

@Serializable
public data class HostComposableInUIWindow(
    val originalInstruction: NavigationInstruction.Open<*>,
) : NavigationKey.SupportsPush, NavigationKey.SupportsPresent

internal val windowForHostingComposable = uiWindowDestination<HostComposableInUIWindow> {
    val binding = controller.bindingForInstruction(navigationKey.originalInstruction)
    requireNotNull(binding) {
        "ComposableNavigationBinding expected for ${navigationKey.originalInstruction.navigationKey}, but got null"
    }
    require(binding is ComposableNavigationBinding<*, *>) {
        "ComposableNavigationBinding expected for ${navigationKey.originalInstruction.navigationKey}, but got ${binding::class}"
    }
    val controller = EnroUIViewController(navigationKey.originalInstruction)
    UIWindow().apply {
        rootViewController = controller
    }
}