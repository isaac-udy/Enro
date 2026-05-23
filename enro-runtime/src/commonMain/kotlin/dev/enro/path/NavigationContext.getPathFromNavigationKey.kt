package dev.enro.path

import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.context.NavigationContext

@ExperimentalEnroApi
public fun NavigationContext<*, *>.getPathFromNavigationKey(
    key: NavigationKey,
): String? {
    return controller.getPathFromNavigationKey(key)
}
