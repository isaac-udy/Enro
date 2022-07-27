package dev.enro

import android.app.Application
import dev.enro.annotations.NavigationComponent
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.navigationController
import dev.enro.core.plugins.EnroLogger

@NavigationComponent
open class TestApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        plugin(EnroLogger())
        plugin(TestPlugin)
    }
}

