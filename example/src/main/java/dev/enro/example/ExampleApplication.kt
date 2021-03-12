package dev.enro.example

import android.app.Application
import dev.enro.annotations.NavigationComponent
import dev.enro.core.*
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.navigationController
import dev.enro.core.plugins.EnroLogger

@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        plugin(EnroLogger())

        override<SplashScreenActivity, Any> {
            animation { DefaultAnimations.none }
        }

    }
}