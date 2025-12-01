package dev.enro

import dev.enro.controller.NavigationModule
import dev.enro.controller.emptyNavigationModule
import platform.UIKit.UIApplication

@Suppress("NOTHING_TO_INLINE")
public inline fun installNavigationController(
    application: UIApplication,
    module: NavigationModule = emptyNavigationModule(),
) : EnroController {
    throw UnsupportedOperationException("Implemented by the compiler")
}