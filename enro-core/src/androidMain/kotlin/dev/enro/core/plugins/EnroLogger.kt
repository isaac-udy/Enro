package dev.enro.core.plugins

import android.util.Log
import dev.enro.core.NavigationHandle

public class EnroLogger : EnroPlugin() {
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