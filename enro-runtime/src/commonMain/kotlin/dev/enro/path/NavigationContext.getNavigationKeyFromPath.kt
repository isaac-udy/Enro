package dev.enro.path

import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.context.NavigationContext

@ExperimentalEnroApi
public fun NavigationContext<*, *>.getNavigationKeyFromPath(
    path: String,
): NavigationKey? {
    val parsedPath = ParsedPath.fromString(path)
    @Suppress("UNCHECKED_CAST")
    val binding = controller.paths.getPathBinding(parsedPath) as? NavigationPathBinding<NavigationKey>
    return binding?.fromPath(parsedPath)
}