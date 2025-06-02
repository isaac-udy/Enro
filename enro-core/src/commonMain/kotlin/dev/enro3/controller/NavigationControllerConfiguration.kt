package dev.enro3.controller

public abstract class NavigationControllerConfiguration(
    internal val module: NavigationModule = createNavigationModule {  }
)