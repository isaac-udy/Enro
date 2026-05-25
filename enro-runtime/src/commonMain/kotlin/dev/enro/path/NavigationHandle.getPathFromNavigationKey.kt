package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi

/**
 * Reverse of [getNavigationKeyFromPath]: serialises [key] back to its
 * registered path string, or returns `null` if [key]'s type has no
 * `@NavigationPath` binding registered on the controller.
 *
 * Use this when you need to emit a URL for the current navigation state
 * — e.g. to update the browser URL bar, build a share link, or persist a
 * deep-link target.
 */
@ExperimentalEnroApi
public fun NavigationHandle<*>.getPathFromNavigationKey(
    key: NavigationKey,
): String? {
    return EnroController.requireInstance().getPathFromNavigationKey(key)
}
