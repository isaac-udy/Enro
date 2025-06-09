package dev.enro.controller

public abstract class NavigationComponentConfiguration(
    public val module: NavigationModule = createNavigationModule {  }
)