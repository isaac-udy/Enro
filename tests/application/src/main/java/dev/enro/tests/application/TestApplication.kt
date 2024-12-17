package dev.enro.tests.application

import android.app.Application
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.MaterialTheme
import dev.enro.animation.direction
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationComponent
import dev.enro.core.NavigationDirection
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.createNavigationController
import dev.enro.destination.fragment.FragmentSharedElements

@NavigationComponent
class TestApplication : Application(), NavigationApplication {
    @OptIn(AdvancedEnroApi::class, ExperimentalEnroApi::class)
    override val navigationController = createNavigationController(
        backConfiguration = EnroBackConfiguration.Predictive
    ) {
        plugin(TestApplicationPlugin)
        composeEnvironment { content ->
            MaterialTheme { content() }
        }

        /**
         * The following plugin is installed specifically to support the example in
         *  [dev.enro.tests.application.fragment.FragmentSharedElementDestination], which has an example of
         *  shared element transitions between a Fragment and Composable NavigationDestination
         */
        plugin(FragmentSharedElements.composeCompatibilityPlugin)

        animations {
            direction(
                direction = NavigationDirection.Push,
                entering = fadeIn() + slideInHorizontally { it / 3 },
                exiting = slideOutHorizontally { -it / 6 },
                returnEntering = slideInHorizontally { -it / 6 },
                returnExiting = fadeOut() + slideOutHorizontally { it / 3 }
            )
        }
    }
}