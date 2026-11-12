package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.EnroController
import dev.enro.NavigationBackstack
import dev.enro.path.getBackstackFromPath

/**
 * Resolves the path of `window.location` to a [NavigationBackstack] through the path bindings
 * of the installed [EnroController]. Returns [default] if no binding matches the path or no
 * [EnroController] is installed. The result is remembered, so the path is read once.
 *
 * Pass the result as the `backstack` argument of `rememberNavigationContainer`:
 *
 * ```kotlin
 * EnroBrowserContent {
 *     val container = rememberNavigationContainer(
 *         backstack = rememberInitialBackstackFromUrl {
 *             backstackOf(Home.asInstance())
 *         },
 *     )
 *     InstallWebHistoryPlugin(container)
 *     NavigationDisplay(container)
 * }
 * ```
 */
@Composable
public fun rememberInitialBackstackFromUrl(
    default: () -> NavigationBackstack,
): NavigationBackstack {
    return remember {
        EnroController.instance?.getBackstackFromPath(BrowserHistory.path) ?: default()
    }
}
