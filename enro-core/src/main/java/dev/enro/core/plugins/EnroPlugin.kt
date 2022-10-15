package dev.enro.core.plugins

import dev.enro.core.NavigationHandle
import dev.enro.core.controller.NavigationController

public abstract class EnroPlugin {
    public open fun onAttached(navigationController: NavigationController) {}
    public open fun onOpened(navigationHandle: NavigationHandle) {}
    public open fun onActive(navigationHandle: NavigationHandle) {}
    public open fun onClosed(navigationHandle: NavigationHandle) {}
}