package dev.enro.core

import dev.enro.core.NavigationDirection.Present
import dev.enro.core.NavigationDirection.Push

public fun NavigationDirection.Companion.defaultDirection(navigationKey: NavigationKey): NavigationDirection {
    return when (navigationKey) {
        is NavigationKey.SupportsPush -> Push
        is NavigationKey.SupportsPresent -> Present
        else -> error("NavigationKey must support either Push or Present to determine a default direction")
    }
}