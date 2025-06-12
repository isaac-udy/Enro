package dev.enrolegacy.core.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import dev.enro.core.controller.NavigationController
import kotlinx.browser.document
import org.w3c.dom.Element

@OptIn(ExperimentalComposeUiApi::class)
public fun EnroViewport(
    controller: NavigationController,
    element: Element = document.body!!
) {
    ComposeViewport(element) {
        Box(modifier = Modifier.fillMaxSize()) {
            controller.windowManager.Render()
        }
    }
}
