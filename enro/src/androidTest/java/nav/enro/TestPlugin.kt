package nav.enro

import nav.enro.core.NavigationHandle
import nav.enro.core.NavigationKey
import nav.enro.core.plugins.EnroPlugin

object TestPlugin : EnroPlugin() {
    var activeKey: NavigationKey? = null

    override fun onActive(navigationHandle: NavigationHandle) {
        activeKey = navigationHandle.key
    }
}