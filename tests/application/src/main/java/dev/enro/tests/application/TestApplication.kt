package dev.enro.tests.application

import android.app.Application
import dev.enro.annotations.NavigationComponent
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.createNavigationController

@NavigationComponent
class TestApplication : Application(), NavigationApplication {
    override val navigationController = createNavigationController {
        plugin(TestApplicationPlugin)
    }
}