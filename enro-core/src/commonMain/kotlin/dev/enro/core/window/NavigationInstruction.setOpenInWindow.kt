package dev.enro.core.window

import dev.enro.core.NavigationInstruction

public fun NavigationInstruction.Open<*>.setOpenInWindow(): NavigationInstruction.Open<*> {
    return this.apply {
        extras.put(NavigationWindowManager.EXTRA_OPEN_IN_WINDOW, true)
    }
}