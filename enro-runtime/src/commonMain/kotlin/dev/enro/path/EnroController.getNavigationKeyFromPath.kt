package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi

/**
 * Looks up a path binding registered on this controller for [path] and returns the
 * resulting [NavigationKey], or `null` if no binding matches.
 */
@ExperimentalEnroApi
public fun EnroController.getNavigationKeyFromPath(path: String): NavigationKey? {
    val parsedPath = ParsedPath.fromString(path)
    @Suppress("UNCHECKED_CAST")
    val binding = paths.getPathBinding(parsedPath) as? NavigationPathBinding<NavigationKey>
    return binding?.fromPath(parsedPath)
}
