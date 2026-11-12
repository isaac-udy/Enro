package dev.enro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Provides the root navigation context for an Enro application rendered with Compose UI in
 * a browser.
 *
 * Usage:
 * ```kotlin
 * fun main() {
 *     MyNavigationComponent.installNavigationController(document)
 *     ComposeViewPort {
 *         EnroBrowserContent {
 *             MyApplicationContent()
 *         }
 *     }
 * }
 * ```
 *
 * @param suppressEscapeAsBack When `true` (the default), EnroBrowserContent consumes an
 *   Escape key press that no composable in [content] consumed, so that Compose
 *   Multiplatform's `BackNavigationEventInput` does not convert the key press into a back
 *   navigation event. A composable in [content] that handles Escape (a dialog or popup)
 *   receives the key press first. Browser back and forward are not affected. When `false`,
 *   an unconsumed Escape key press is a back navigation event.
 */
@Composable
public fun EnroBrowserContent(
    suppressEscapeAsBack: Boolean = true,
    content: @Composable EnroBrowserScope.() -> Unit,
) {
    EnroBrowserRoot {
        if (suppressEscapeAsBack) {
            // onKeyEvent runs in the bubble phase, so focused content receives Escape first.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent { event ->
                        event.type == KeyEventType.KeyDown && event.key == Key.Escape
                    },
            ) {
                content()
            }
        } else {
            content()
        }
    }
}
