package dev.enro.example

import android.app.Application
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset
import dagger.hilt.android.HiltAndroidApp
import dev.enro.annotations.NavigationComponent
import dev.enro.core.DefaultAnimations
import dev.enro.core.NavigationAnimation
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.navigationController
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

        override<ComposeSimpleExampleDestination, ComposeSimpleExampleDestination> {
            animation {
                NavigationAnimation.Composable(
                    forView = DefaultAnimations.push,
                    enter = fadeIn(tween(700)),
                    exit = fadeOut(tween(700)),
                )
            }
            closeAnimation {
                NavigationAnimation.Composable(
                    forView = DefaultAnimations.close,
                    enter = slideIn(tween(700)) { IntOffset(0, 300) },
                    exit = slideOut(tween(700)) { IntOffset(0, 300) },
                )
            }
        }
        composeEnvironment { content ->
            EnroExampleTheme(content)
        }
    }
}
