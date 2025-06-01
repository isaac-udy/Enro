package dev.enro3.controller

import dev.enro3.EnroController

// Marked as internal, but is used in generated code with a @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
public fun internalCreateEnroController(
    builder: NavigationModule.BuilderScope.() -> Unit = {},
) : EnroController {
    val module = createNavigationModule(builder)
    return EnroController().apply {
        addModule(defaultNavigationModule)
        addModule(module)
    }
}