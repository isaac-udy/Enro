package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey

/**
 * Resolves [path] through the `@NavigationPath` bindings registered on the installed
 * [EnroController] and returns the matching [NavigationKey], or `null` if no binding
 * matches. Throws [IllegalStateException] if no [EnroController] is installed.
 */
public fun NavigationHandle<*>.getNavigationKeyFromPath(
    path: String,
): NavigationKey? {
    return EnroController.requireInstance().getNavigationKeyFromPath(path)
}
