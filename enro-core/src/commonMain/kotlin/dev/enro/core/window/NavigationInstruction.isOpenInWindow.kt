package dev.enro.core.window

import dev.enro.core.NavigationInstruction

public fun NavigationInstruction.Open<*>.isOpenInWindow(): Boolean {
    return this.extras.get(NavigationWindowManager.EXTRA_OPEN_IN_WINDOW) as? Boolean ?: false
}