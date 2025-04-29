
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import dev.enro.animation.direction
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.createNavigationController
import dev.enro.tests.application.ExampleApplicationWindow

fun main() = application {
    val controller = remember {
        val controller = createNavigationController {
            EnroExampleAppNavigation().invoke(this)
            animations {
                direction(
                    direction = NavigationDirection.Push,
                    entering = fadeIn() + slideInHorizontally { 192 },
                    exiting = slideOutHorizontally { -64 },
                    returnEntering = slideInHorizontally { -64 },
                    returnExiting = fadeOut() + slideOutHorizontally { 192 }
                )
            }
        }
        controller.windowManager.open(NavigationInstruction.Present(ExampleApplicationWindow))
        controller
    }
    with(controller.windowManager) {
        Render()
    }
}
