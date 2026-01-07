package dev.enro.platform.desktop

import dev.enro.EnroController
import dev.enro.NavigationKey

public fun EnroController.openWindow(
    window: RootWindow<NavigationKey>,
) {
    rootContextRegistry.register(window.navigationContext)
}

