@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.tests.application.SelectDestination
import dev.enro.tests.application.TestApplicationComponent
import dev.enro.tests.application.TestApplicationComponentNavigation
import dev.enro.tests.application.installNavigationController
import dev.enro.ui.EnroBrowserContent
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.browser.document
import org.jetbrains.compose.resources.configureWebResources

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    TestApplicationComponent.installNavigationController(document)
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    ComposeViewport {
        EnroBrowserContent {
            val container = rememberNavigationContainer(
                backstack = backstackOf(SelectDestination().asInstance())
            )
            NavigationDisplay(container)
        }
    }
}
