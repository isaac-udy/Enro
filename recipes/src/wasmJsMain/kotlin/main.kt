@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.recipes.RecipesComponent
import dev.enro.recipes.SelectRecipe
import dev.enro.recipes.installNavigationController
import dev.enro.ui.EnroBrowserContent
import dev.enro.ui.InstallWebHistoryPlugin
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberInitialBackstackFromUrl
import dev.enro.ui.rememberNavigationContainer
import kotlinx.browser.document
import org.jetbrains.compose.resources.configureWebResources

@OptIn(ExperimentalComposeUiApi::class, ExperimentalEnroApi::class)
fun main() {
    RecipesComponent.installNavigationController(document)
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }

    ComposeViewport {
        EnroBrowserContent {
            val container = rememberNavigationContainer(
                backstack = rememberInitialBackstackFromUrl {
                    backstackOf(SelectRecipe.asInstance())
                },
            )
            InstallWebHistoryPlugin(container)
            NavigationDisplay(container)
        }
    }
}
