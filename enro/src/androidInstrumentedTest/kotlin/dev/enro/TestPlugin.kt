package dev.enro

import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.plugins.EnroPlugin

object TestPlugin : EnroPlugin() {
    var activeKey: NavigationKey? = null

    override fun onActive(navigationHandle: NavigationHandle) {
        activeKey = navigationHandle.key
    }
}