package dev.enro.path

import dev.enro.NavigationKey
import dev.enro.context.NavigationContext

public fun NavigationContext<*, *>.getPathFromNavigationKey(
    key: NavigationKey,
): String? {
    return controller.getPathFromNavigationKey(key)
}
