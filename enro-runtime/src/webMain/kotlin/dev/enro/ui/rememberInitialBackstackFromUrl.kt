package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.EnroController
import dev.enro.NavigationBackstack
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.path.getBackstackFromPath

/**
 * Reads the current `window.location` once at composition and tries to resolve it
 * to a [NavigationBackstack] using the controller's path bindings. If no binding
 * matches (or no [EnroController] is installed yet), returns [default] instead.
 *
 * Intended to be used as the `backstack` argument of `rememberNavigationContainer`
 * so that opening a deep-link URL on a cold load lands on the right destination:
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
@ExperimentalEnroApi
@Composable
public fun rememberInitialBackstackFromUrl(
    default: () -> NavigationBackstack,
): NavigationBackstack {
    return remember {
        EnroController.instance?.getBackstackFromPath(BrowserHistory.path) ?: default()
    }
}
