package dev.enro

import dev.enro.controller.NavigationModule
import dev.enro.controller.emptyNavigationModule

@Suppress("NOTHING_TO_INLINE")
public inline fun installNavigationController(
    module: NavigationModule = emptyNavigationModule(),
) : EnroController {
    throw UnsupportedOperationException("Implemented by the compiler")
}