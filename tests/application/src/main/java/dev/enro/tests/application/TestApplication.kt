package dev.enro.tests.application

import android.app.Application
import androidx.compose.material.MaterialTheme
import dev.enro.annotations.NavigationComponent
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.createNavigationController

@NavigationComponent
class TestApplication : Application(), NavigationApplication {
    override val navigationController = createNavigationController {
        plugin(TestApplicationPlugin)
        composeEnvironment { content ->
            MaterialTheme { content() }
        }
    }
}