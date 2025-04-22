
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import dev.enro.annotations.NavigationComponent
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.createNavigationController
import dev.enro.tests.application.ExampleApplicationWindow

@NavigationComponent
object EnroExampleApp

fun main() = application {
    val controller = remember {
        val controller = createNavigationController {
            EnroExampleAppNavigation().invoke(this)
        }
        controller.windowManager.open(NavigationInstruction.Present(ExampleApplicationWindow))
        controller
    }
    with(controller.windowManager) {
        Render()
    }
}
