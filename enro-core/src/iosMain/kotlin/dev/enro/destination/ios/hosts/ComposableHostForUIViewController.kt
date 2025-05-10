package dev.enro.destination.ios.hosts

import NavigationHostFactory
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.asPresent
import dev.enro.core.asPush
import dev.enro.destination.compose.ComposableDestination
import dev.enro.destination.ios.UIViewControllerNavigationBinding

internal class ComposableHostForUIViewController : NavigationHostFactory<ComposableDestination>(ComposableDestination::class) {
    override fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): Boolean {
        val binding = requireNotNull(navigationContext.controller.bindingForInstruction(instruction))
        return binding is UIViewControllerNavigationBinding<*, *>
    }

    override fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*> {
        if (!supports(navigationContext, instruction)) cannotCreateHost(instruction)
        return when (instruction.navigationDirection) {
            NavigationDirection.Present -> HostUIViewControllerInCompose(instruction).asPresent()
            NavigationDirection.Push -> HostUIViewControllerInCompose(instruction).asPush()
        }
    }
}
