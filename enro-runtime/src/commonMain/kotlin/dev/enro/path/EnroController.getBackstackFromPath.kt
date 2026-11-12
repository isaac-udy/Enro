package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationBackstack
import dev.enro.asInstance
import dev.enro.backstackOf

/**
 * Resolves [path] through the path bindings registered on this controller and returns a
 * [NavigationBackstack] with one entry. Returns `null` if no binding matches [path] or
 * resolving [path] throws.
 *
 * ```kotlin
 * val container = rememberNavigationContainer(
 *     backstack = controller.getBackstackFromPath(deeplinkUrl)
 *         ?: backstackOf(Home.asInstance()),
 * )
 * ```
 */
public fun EnroController.getBackstackFromPath(path: String): NavigationBackstack? {
    val key = runCatching { getNavigationKeyFromPath(path) }.getOrNull() ?: return null
    return backstackOf(key.asInstance())
}
