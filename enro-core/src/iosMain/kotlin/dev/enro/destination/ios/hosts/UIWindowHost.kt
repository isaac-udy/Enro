package dev.enro.destination.ios.hosts

import NavigationHostFactory
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.asPresent
import dev.enro.destination.compose.ComposableNavigationBinding
import dev.enro.destination.flow.ManagedFlowNavigationBinding
import dev.enro.destination.ios.UIViewControllerNavigationBinding
import dev.enro.destination.ios.UIWindowNavigationBinding
import platform.UIKit.UIWindow

internal class UIWindowHost : NavigationHostFactory<UIWindow>(UIWindow::class) {
    override fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): Boolean {
        val binding = requireNotNull(navigationContext.controller.bindingForInstruction(instruction))
        return when(binding) {
            is UIWindowNavigationBinding<*, *> -> true
            is UIViewControllerNavigationBinding<*, *> -> true
            is ComposableNavigationBinding<*, *> -> true
            is ManagedFlowNavigationBinding<*, *> -> true
            else -> false
        }
    }

    override fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*> {
        val binding = requireNotNull(navigationContext.controller.bindingForInstruction(instruction))
        return when (binding) {
            is UIWindowNavigationBinding<*, *> -> instruction
            is UIViewControllerNavigationBinding<*, *> -> HostUIViewControllerInUIWindow(instruction).asPresent()
            is ComposableNavigationBinding<*, *> -> HostComposableInUIWindow(instruction).asPresent()
            is ManagedFlowNavigationBinding<*, *> -> HostComposableInUIWindow(instruction).asPresent()
            else -> cannotCreateHost(instruction)
        }
    }
}