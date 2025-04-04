package dev.enro.core.plugins

import dev.enro.core.NavigationHandle
import dev.enro.core.internal.EnroLog

public class EnroLogger : EnroPlugin() {
    override fun onOpened(navigationHandle: NavigationHandle) {
        EnroLog.debug("Opened: ${navigationHandle.key}")
    }

    override fun onActive(navigationHandle: NavigationHandle) {
        EnroLog.debug("Active: ${navigationHandle.key}")
    }

    override fun onClosed(navigationHandle: NavigationHandle) {
        EnroLog.debug("Closed: ${navigationHandle.key}")
    }
}