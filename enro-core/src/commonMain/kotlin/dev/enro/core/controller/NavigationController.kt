package dev.enro.core.controller

import dev.enro.core.EnroConfig

public expect class NavigationController internal constructor()  {
    internal val dependencyScope: EnroDependencyScope

    internal var config: EnroConfig
        private set
}