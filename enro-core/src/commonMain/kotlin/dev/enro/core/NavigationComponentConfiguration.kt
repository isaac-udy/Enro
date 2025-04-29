package dev.enro.core

import dev.enro.core.controller.NavigationModule
import dev.enro.core.controller.createNavigationModule

public abstract class NavigationComponentConfiguration(
    public val module: NavigationModule = createNavigationModule { }
)