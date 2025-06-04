package dev.enro.plugin

import dev.enro.EnroController
import dev.enro.NavigationContainer
import dev.enro.NavigationHandle
import dev.enro.NavigationTransition

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