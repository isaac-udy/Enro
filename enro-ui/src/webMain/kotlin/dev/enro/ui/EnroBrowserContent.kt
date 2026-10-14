package dev.enro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * A composable that provides the root navigation context for a browser-based Enro application.
 *
 * This is the main entry point for using Enro with Compose UI in a browser. It sets up the
 * required navigation context, view model store, and navigation handle that are needed for
 * Enro navigation to work.
 *
 * Usage:
 * ```kotlin
 * fun main() {
 *     MyNavigationComponent.installNavigationController(document)
 *     ComposeViewPort {
 *         EnroBrowserContent {
 *             // Your app content here
 *             MyApplicationContent()
 *         }
 *     }
 * }
 * ```
 *
 * @param suppressEscapeAsBack When `true` (the default), pressing the
 *   Escape key inside the Compose composition is consumed before
 *   Compose Multiplatform's built-in `BackNavigationEventInput` can
 *   translate it into a back-navigation event. Browser back/forward via
 *   the history API is unaffected (it routes through `popstate`, not the
 *   navigation event dispatcher). Set to `false` if your app deliberately
 *   wants Escape→back behaviour.
 * @param content The composable content of your application
 */
@Composable
public fun EnroBrowserContent(
    suppressEscapeAsBack: Boolean = true,
    content: @Composable EnroBrowserScope.() -> Unit,
) {
    EnroBrowserRoot {
        if (suppressEscapeAsBack) {
            // Consumes Escape KeyDown via Compose's preview key chain
            // (outer → inner) before Compose Multiplatform's
            // BackNavigationEventInput can translate it into a back event.
            // Inner Popups/Dialogs run in their own focus tree and still
            // see Escape — only the main composition's Escape is swallowed.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onPreviewKeyEvent { event ->
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
