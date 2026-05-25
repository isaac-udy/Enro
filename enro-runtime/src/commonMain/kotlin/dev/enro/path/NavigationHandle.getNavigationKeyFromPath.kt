package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi

/**
 * Resolves [path] against the controller's registered `@NavigationPath`
 * bindings and returns the matching [NavigationKey], or `null` if no
 * binding matches.
 *
 * Use this when a destination needs to react to an external URL /
 * deep-link string — e.g. when handling a clicked notification, a
 * `data:` intent, or a web-routed message. The resolved key can be
 * handed to [open] / [closeAndReplaceWith] like any other key.
 *
 * The handle receiver scopes the lookup to the currently-installed
 * [EnroController]; if you're outside a destination, use the controller
 * extensions directly.
 */
@ExperimentalEnroApi
public fun NavigationHandle<*>.getNavigationKeyFromPath(
    path: String,
): NavigationKey? {
    return EnroController.requireInstance().getNavigationKeyFromPath(path)
}
