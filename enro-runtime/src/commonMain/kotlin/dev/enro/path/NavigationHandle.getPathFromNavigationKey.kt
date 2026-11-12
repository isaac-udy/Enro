package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey

/**
 * Returns the path of [key], or `null` if the installed [EnroController] has no
 * `@NavigationPath` binding for the type of [key]. [getNavigationKeyFromPath] is the
 * reverse. Throws [IllegalStateException] if no [EnroController] is installed.
 */
public fun NavigationHandle<*>.getPathFromNavigationKey(
    key: NavigationKey,
): String? {
    return EnroController.requireInstance().getPathFromNavigationKey(key)
}
