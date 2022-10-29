package dev.enro.core.internal.handle

import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.EnroDependencyContainer
import dev.enro.core.internal.EnroDependencyScope

internal class NavigationHandleScope(
    navigationController: NavigationController
)  : EnroDependencyScope {
    override val container: EnroDependencyContainer = EnroDependencyContainer(
        parentScope = navigationController.dependencyScope,
        registration = {

        }
    )
}