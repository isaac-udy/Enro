package dev.enro

import android.app.Application
import dev.enro.controller.NavigationComponentConfiguration

public inline fun <reified T : NavigationComponentConfiguration> installNavigationController(
    application: Application,
) : EnroController {
    throw UnsupportedOperationException("Implemented by the compiler")
}