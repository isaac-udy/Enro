package dev.enro.example

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.enro.annotations.NavigationComponent
import dev.enro.core.DefaultAnimations
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.navigationController
import dev.enro.core.createOverride
import dev.enro.core.createSharedElementOverride
import dev.enro.core.plugins.EnroLogger

@HiltAndroidApp
@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        plugin(EnroLogger())

        override<SplashScreenActivity, Any> {
            animation { DefaultAnimations.none }
        }
        override(
            createSharedElementOverride<RequestExampleFragment, RequestStringFragment>(
                listOf(R.id.requestStringButton to R.id.sendResultButton)
            )
        )
    }
}