package dev.enro.core.controller

import android.app.Application
import dev.enro.platform.enroController

@Deprecated(
    message = "Application.navigationController has been renamed to Application.enroController. Please use enroController instead.",
    replaceWith = ReplaceWith("enroController", "dev.enro.platform.enroController"),
    level = DeprecationLevel.WARNING
)
public val Application.navigationController: NavigationController get() = enroController
