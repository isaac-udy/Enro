package dev.enro.core

import androidx.activity.ComponentActivity
import dev.enro.context.ContainerContext
import dev.enro.context.NavigationContext
import dev.enro.platform.navigationContext

public val ComponentActivity.containerManager: ContainerManager get() {
    return ContainerManager(navigationContext)
}

public class ContainerManager(
    private val context: NavigationContext.WithContainerChildren<*>
) {
    public val containers: List<ContainerContext> get() = context.children
}