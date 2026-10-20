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
 * @param suppressEscapeAsBack When `true` (the default), an Escape key
 *   press that nothing in the composition handles is consumed before
 *   Compose Multiplatform's built-in `BackNavigationEventInput` can
 *   translate it into a back-navigation event. Content that handles
 *   Escape itself (a dialog or popup closing) still receives it first.
 *   Browser back/forward via
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
            // Bubble phase: focused content handles Escape first; the web window
            // only turns Escape into back when the composition leaves it unconsumed.
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
