package dev.enro.controller

import dev.enro.EnroController
import dev.enro.platform.platformNavigationModule

// Marked as internal, but is used in generated code with a @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
public fun internalCreateEnroController(
    builder: NavigationModule.BuilderScope.() -> Unit = {},
) : EnroController {
    val module = createNavigationModule(builder)
    return EnroController().apply {
        addModule(defaultNavigationModule)
        addModule(platformNavigationModule)
        addModule(module)
    }
}
