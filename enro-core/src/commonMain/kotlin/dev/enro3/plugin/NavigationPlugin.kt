package dev.enro3.plugin

import dev.enro3.EnroController
import dev.enro3.NavigationContainer
import dev.enro3.NavigationHandle
import dev.enro3.NavigationTransition

public abstract class NavigationPlugin {
    public open fun onAttached(controller: EnroController) {}
    public open fun onDetached(controller: EnroController) {}

    public open fun onTransitionApplied(
        container: NavigationContainer,
        transition: NavigationTransition,
    ) {}

    public open fun onOpened(navigationHandle: NavigationHandle<*>) {}
    public open fun onActive(navigationHandle: NavigationHandle<*>) {}
    public open fun onClosed(navigationHandle: NavigationHandle<*>) {}
}