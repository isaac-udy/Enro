// Web snippets for docs/ghpages/docs/getting-started/installation.md
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.docs.gettingstarted.installation

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.EnroBrowserContent
import dev.enro.ui.InstallWebHistoryPlugin
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.browser.document

// region docs:installation/web
fun main() {
    MyComponent.installNavigationController(document)

    ComposeViewport {
        EnroBrowserContent {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            InstallWebHistoryPlugin(container)
            NavigationDisplay(state = container)
        }
    }
}
// endregion
