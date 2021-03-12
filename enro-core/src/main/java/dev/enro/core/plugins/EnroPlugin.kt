package dev.enro.core.plugins

import dev.enro.core.NavigationHandle
import dev.enro.core.controller.NavigationController

abstract class EnroPlugin {
    open fun onAttached(navigationController: NavigationController) {}
    open fun onOpened(navigationHandle: NavigationHandle) {}
    open fun onActive(navigationHandle: NavigationHandle) {}
    open fun onClosed(navigationHandle: NavigationHandle) {}
}