package nav.enro.core.controller

import android.util.Log
import nav.enro.core.NavigationHandle
import nav.enro.core.NavigationKey

abstract class EnroPlugin {
    open fun onAttached(navigationController: NavigationController) {}
    open fun onOpened(navigationHandle: NavigationHandle<*>) {}
    open fun onActive(navigationHandle: NavigationHandle<*>) {}
    open fun onClosed(navigationHandle: NavigationHandle<*>) {}
}