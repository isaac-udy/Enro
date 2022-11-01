package dev.enro.example

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.enro.annotations.NavigationComponent
import dev.enro.core.DefaultAnimations
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.createNavigationController
import dev.enro.core.createSharedElementOverride
import dev.enro.core.plugins.EnroLogger

@HiltAndroidApp
@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = createNavigationController {
        plugin(EnroLogger())

        override<SplashScreenActivity, Any> {
            animation {
                DefaultAnimations.present
            }
        }

        composeEnvironment { content ->
            EnroExampleTheme(content)
        }
    }
}
