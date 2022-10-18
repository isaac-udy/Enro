package dev.enro

import android.app.Application
import android.util.Log
import dev.enro.annotations.NavigationComponent
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.navigationController
import dev.enro.core.plugins.EnroLogger
import dev.enro.core.plugins.EnroPlugin

@NavigationComponent
open class TestApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        plugin(EnroLogger())
        plugin(TestPlugin)
        plugin(object : EnroPlugin() {
            override fun onOpened(navigationHandle: NavigationHandle) {
                Log.e(
                    "EnroResultHandles",
                    "Opened with ${getActiveEnroResultChannels().size} active result channels"
                )
            }

            override fun onClosed(navigationHandle: NavigationHandle) {
                Log.e(
                    "EnroResultHandles",
                    "Closed with ${getActiveEnroResultChannels().size} active result channels"
                )
            }
        })
    }
}

