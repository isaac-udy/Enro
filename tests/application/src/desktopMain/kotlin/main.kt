
import androidx.compose.ui.window.application
import dev.enro.core.asPresent
import dev.enro.tests.application.EnroComponent
import dev.enro.tests.application.ExampleApplicationWindow
import dev.enro.tests.application.rememberNavigationController

fun main() = application {
    val controller = EnroComponent.rememberNavigationController(
        root = ExampleApplicationWindow.asPresent()
    )
    controller.windowManager.Render()
}
