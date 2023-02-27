package dev.enro.example

import android.app.Application
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.ui.unit.IntOffset
import dagger.hilt.android.HiltAndroidApp
import dev.enro.annotations.NavigationComponent
import dev.enro.core.DefaultAnimations
import dev.enro.core.NavigationAnimation
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.createNavigationController
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

//        override<ExampleComposableScreenDestination, ExampleComposableScreenDestination> {
//            animation {
//                NavigationAnimationTransition(open, open)
//            }
//            closeAnimation {
//                NavigationAnimationTransition(close, close)
//            }
//        }
        composeEnvironment { content ->
            EnroExampleTheme(content)
        }
    }
}

val open =
    NavigationAnimation.Composable(
        forView = DefaultAnimations.ForView.pushEnter,
        enter = fadeIn(tween(700, delayMillis = 700)),
        exit = fadeOut(tween(700)),
    )

val close = NavigationAnimation.Composable(
    forView = DefaultAnimations.ForView.pushExit,
    enter = slideIn(tween(700, delayMillis = 500)) { IntOffset(0, 300) },
    exit = fadeOut(tween(500)),
)
