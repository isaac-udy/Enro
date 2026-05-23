package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationBackstack
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.asInstance
import dev.enro.backstackOf

/**
 * Resolves [path] to a single-entry [NavigationBackstack] using this controller's
 * registered path bindings. Returns `null` if no binding matches the path.
 *
 * Useful for deriving an initial backstack from a deep-link URL (browser address
 * bar on cold load, Android intent extra, iOS universal link payload, etc.).
 *
 * For most callers, the resulting backstack will be passed to a navigation
 * container as its initial state, e.g.:
 *
 * ```kotlin
 * val container = rememberNavigationContainer(
 *     backstack = controller.getBackstackFromPath(deeplinkUrl)
 *         ?: backstackOf(Home.asInstance()),
 * )
 * ```
 */
@ExperimentalEnroApi
public fun EnroController.getBackstackFromPath(path: String): NavigationBackstack? {
    val key = runCatching { getNavigationKeyFromPath(path) }.getOrNull() ?: return null
    return backstackOf(key.asInstance())
}
