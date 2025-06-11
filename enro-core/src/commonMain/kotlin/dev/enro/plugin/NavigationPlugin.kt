package dev.enro.plugin

import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.context.RootContext

public abstract class NavigationPlugin {
    public open fun onAttached(controller: EnroController) {}
    public open fun onDetached(controller: EnroController) {}

    public fun onRootContextAttached(context: RootContext) {}
    public fun onRootContextDetached(context: RootContext) {}

    public open fun onOpened(navigationHandle: NavigationHandle<*>) {}
    public open fun onActive(navigationHandle: NavigationHandle<*>) {}
    public open fun onClosed(navigationHandle: NavigationHandle<*>) {}
}