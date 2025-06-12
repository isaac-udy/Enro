package dev.enrolegacy.destination.ios.hosts

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.destination.ios.UIViewControllerNavigationBinding
import dev.enro.destination.ios.uiWindowDestination
import kotlinx.serialization.Serializable
import platform.UIKit.UIWindow

@Serializable
public data class HostUIViewControllerInUIWindow(
    val originalInstruction: NavigationInstruction.Open<*>,
) : NavigationKey.SupportsPush, NavigationKey.SupportsPresent

internal val windowForHostingUIViewController = uiWindowDestination<HostUIViewControllerInUIWindow> {
    val binding = controller.bindingForInstruction(navigationKey.originalInstruction)
    requireNotNull(binding) {
        "UIViewControllerNavigationBinding expected, but got null"
    }
    require(binding is UIViewControllerNavigationBinding<*, *>) {
        "UIViewControllerNavigationBinding expected, but got ${binding::class}"
    }
    val controller = binding.constructDestination()
    UIWindow().apply {
        rootViewController = controller
    }
}