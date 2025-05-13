package dev.enro.core.window

import dev.enro.core.NavigationKey
import dev.enro.core.withExtra

public fun <T: NavigationKey> T.setOpenInWindow(): NavigationKey.WithExtras<T> {
    return withExtra(NavigationWindowManager.EXTRA_OPEN_IN_WINDOW, true)
}

public fun <T: NavigationKey> NavigationKey.WithExtras<T>.setOpenInWindow(): NavigationKey.WithExtras<T> {
    return withExtra(NavigationWindowManager.EXTRA_OPEN_IN_WINDOW, true)
}
