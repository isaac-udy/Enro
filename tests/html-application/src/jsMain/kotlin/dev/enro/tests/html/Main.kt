package dev.enro.tests.html

import dev.enro.annotations.NavigationComponent
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.html.EnroHtmlContent
import dev.enro.html.HtmlNavigationDisplay
import dev.enro.ui.InstallWebHistoryPlugin
import dev.enro.ui.rememberInitialBackstackFromUrl
import dev.enro.ui.rememberNavigationContainer
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable

@NavigationComponent
object HtmlApplicationComponent : NavigationComponentConfiguration()

fun main() {
    HtmlApplicationComponent.installNavigationController(document)
    renderComposable(rootElementId = "root") {
        EnroHtmlContent {
            val container = rememberNavigationContainer(
                backstack = rememberInitialBackstackFromUrl { backstackOf(ShoppingList.asInstance()) },
            )
            InstallWebHistoryPlugin(container)
            HtmlNavigationDisplay(container)
        }
    }
}
