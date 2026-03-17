package dev.enro.core.controller

import dev.enro.controller.NavigationModule

public fun createNavigationModule(block: NavigationModule.BuilderScope.() -> Unit): NavigationModule {
    return dev.enro.controller.createNavigationModule(block)
}