package dev.enro.core.controller

import android.app.Application
import dev.enro.core.EnroException

public val Application.navigationController: NavigationController
    get() {
        return NavigationController.navigationController
            ?: error("NavigationController is null")
    }

public val NavigationController.isInAndroidContext: Boolean
    get() = NavigationController.platformReference is Application

internal val NavigationController.application: Application
    get() {
        return NavigationController.platformReference as? Application
            ?: throw EnroException.NavigationControllerIsNotAttached("NavigationController is not attached to an Application")
    }