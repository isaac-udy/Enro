@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import dev.enro.core.asPresent
import dev.enro.core.window.EnroViewport
import dev.enro.tests.application.EnroComponent
import dev.enro.tests.application.ExampleApplicationWindow
import dev.enro.tests.application.installNavigationController
import kotlinx.browser.document
import org.jetbrains.compose.resources.configureWebResources

fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    val controller = EnroComponent.installNavigationController(
        document = document,
        root = ExampleApplicationWindow.asPresent(),
    )
    runCatching {
        EnroViewport(
            controller = controller,
        )
    }.onFailure {
        it.printStackTrace()
    }
}
