package dev.enro.path

import dev.enro.NavigationKey
import dev.enro.context.NavigationContext

public fun NavigationContext<*, *>.getNavigationKeyFromPath(
    path: String,
): NavigationKey? {
    return controller.getNavigationKeyFromPath(path)
}
