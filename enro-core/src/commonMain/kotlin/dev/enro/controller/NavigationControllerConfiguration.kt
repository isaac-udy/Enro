package dev.enro.controller

public abstract class NavigationControllerConfiguration(
    internal val module: NavigationModule = createNavigationModule {  }
)