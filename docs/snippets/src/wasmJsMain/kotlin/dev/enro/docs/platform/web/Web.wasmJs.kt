// Snippets for docs/ghpages/docs/platform/web.md
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.docs.platform.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.EnroBrowserContent
import dev.enro.ui.InstallWebHistoryPlugin
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberInitialBackstackFromUrl
import dev.enro.ui.rememberNavigationContainer
import kotlinx.browser.document

// region docs:web/install
fun main() {
    MyComponent.installNavigationController(document)

    ComposeViewport {
        EnroBrowserContent {
            val container = rememberNavigationContainer(
                backstack = rememberInitialBackstackFromUrl {
                    backstackOf(Home.asInstance())
                },
            )
            InstallWebHistoryPlugin(container)
            NavigationDisplay(state = container)
        }
    }
}
// endregion
