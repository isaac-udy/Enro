@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import dev.enro.core.window.EnroViewport
import dev.enro.tests.application.EnroComponent
import dev.enro.tests.application.installNavigationController
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.resources.configureWebResources

fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }

    runCatching {
        val controller = EnroComponent.installNavigationController(
            document = document,
        )

        val path = window.location
        val specificPath = window.location.href
            .removePrefix(path.origin)
            .removeSuffix(path.hash)
        val instruction = controller.instructionForPath(specificPath)
        if (instruction != null) {
            controller.windowManager.open(instruction)
        }

        EnroViewport(
            controller = controller,
        )
    }.onFailure {
        it.printStackTrace()
    }
}
