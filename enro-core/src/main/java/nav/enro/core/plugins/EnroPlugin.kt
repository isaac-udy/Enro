package nav.enro.core.plugins

import nav.enro.core.NavigationHandle
import nav.enro.core.controller.NavigationController

abstract class EnroPlugin {
    open fun onAttached(navigationController: NavigationController) {}
    open fun onOpened(navigationHandle: NavigationHandle) {}
    open fun onActive(navigationHandle: NavigationHandle) {}
    open fun onClosed(navigationHandle: NavigationHandle) {}
}