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

class EnroLogger : EnroPlugin() {
    override fun onOpened(navigationHandle: NavigationHandle<*>) {
        Log.d("NavigationController", "Opened: ${navigationHandle.key}")
    }

    override fun onActive(navigationHandle: NavigationHandle<*>) {
        Log.d("NavigationController", "Active: ${navigationHandle.key}")
    }

    override fun onClosed(navigationHandle: NavigationHandle<*>) {
        Log.d("NavigationController", "Closed: ${navigationHandle.key}")
    }
}