package dev.enro

import android.app.Application
import dev.enro.controller.NavigationModule
import dev.enro.controller.emptyNavigationModule

@Suppress("NOTHING_TO_INLINE")
public inline fun installNavigationController(
    application: Application,
    module: NavigationModule = emptyNavigationModule(),
) : EnroController {
    throw UnsupportedOperationException("Implemented by the compiler")
}