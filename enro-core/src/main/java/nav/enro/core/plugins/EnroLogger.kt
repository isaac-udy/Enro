package nav.enro.core.plugins

import android.util.Log
import nav.enro.core.NavigationHandle

class EnroLogger : EnroPlugin() {
    override fun onOpened(navigationHandle: NavigationHandle) {
        Log.d("Enro", "Opened: ${navigationHandle.key}")
    }

    override fun onActive(navigationHandle: NavigationHandle) {
        Log.d("Enro", "Active: ${navigationHandle.key}")
    }

    override fun onClosed(navigationHandle: NavigationHandle) {
        Log.d("Enro", "Closed: ${navigationHandle.key}")
    }
}