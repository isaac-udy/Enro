package dev.enro.platform

import dev.enro.compat.compatNavigationModule
import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule

internal actual val platformNavigationModule: NavigationModule = createNavigationModule {
    module(compatNavigationModule)
}
