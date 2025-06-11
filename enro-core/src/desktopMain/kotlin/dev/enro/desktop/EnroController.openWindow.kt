package dev.enro.desktop

import dev.enro.EnroController

public fun EnroController.openWindow(
    window: RootWindow,
) {
    rootContextRegistry.register(window.context)
}

