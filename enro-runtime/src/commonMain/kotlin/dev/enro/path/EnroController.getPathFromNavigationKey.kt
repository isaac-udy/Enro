package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationKey

/**
 * Returns a URL-style path string for [key] using the first registered binding whose
 * keyType matches, or `null` if no binding exists for [key]'s type.
 */
public fun EnroController.getPathFromNavigationKey(key: NavigationKey): String? {
    val binding = paths.getPathBindingForKey(key) ?: return null
    return binding.toPath(key)
}
