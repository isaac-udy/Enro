@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.tests.application.SelectDestination
import dev.enro.tests.application.TestApplicationComponent
import dev.enro.tests.application.installNavigationController
import dev.enro.ui.EnroBrowserContent
import dev.enro.ui.InstallWebHistoryPlugin
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import embedded_enro.tests.application.generated.resources.NotoEmoji_SemiBold
import embedded_enro.tests.application.generated.resources.Res
import kotlinx.browser.document
import org.jetbrains.compose.resources.configureWebResources
import org.jetbrains.compose.resources.getFontResourceBytes
import org.jetbrains.compose.resources.rememberResourceEnvironment

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    TestApplicationComponent.installNavigationController(document)
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }

    ComposeViewport {
        val fontFamilyResolver = LocalFontFamilyResolver.current
        val resourceEnvironment = rememberResourceEnvironment()
        LaunchedEffect(Unit) {
            val notoEmojisBytes = getFontResourceBytes(resourceEnvironment, Res.font.NotoEmoji_SemiBold)
            val fontFamily = FontFamily(listOf(Font("NotoEmoji", notoEmojisBytes)))
            fontFamilyResolver.preload(fontFamily)
        }
        EnroBrowserContent {
            val container = rememberNavigationContainer(
                backstack = backstackOf(SelectDestination().asInstance())
            )
            InstallWebHistoryPlugin(container)
            NavigationDisplay(container)
        }
    }
}
