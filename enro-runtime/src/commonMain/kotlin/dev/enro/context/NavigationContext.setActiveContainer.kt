package dev.enro.context

import dev.enro.NavigationContainer
import dev.enro.ui.NavigationContainerState

private fun NavigationContext<*, ContainerContext>.setActiveContainerId(
    id: String,
) {
    when (this) {
        is ContainerContext -> return
        is DestinationContext<*> -> setActiveContainer(id)
        is RootContext -> setActiveContainer(id)
    }
}

public fun NavigationContext<*, ContainerContext>.setActiveContainer(child: ContainerContext) {
    setActiveContainerId(child.id)
}

public fun NavigationContext<*, ContainerContext>.setActiveContainer(child: NavigationContainerState) {
    setActiveContainerId(child.key.name)
}

public fun NavigationContext<*, ContainerContext>.setActiveContainer(child: NavigationContainer) {
    setActiveContainerId(child.key.name)
}