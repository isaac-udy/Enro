package dev.enro.core.controller

import android.app.Application
import dev.enro.core.EnroException

public val Application.navigationController: NavigationController
    get() {
        synchronized(this) {
            if (this is NavigationApplication) return navigationController
            val bound = NavigationController.navigationControllerBindings[this]
            if (bound == null) {
                val navigationController = NavigationController()
                NavigationController.navigationControllerBindings[this] = NavigationController()
                navigationController.install(object : NavigationApplication {
                    override val navigationController: NavigationController
                        get() = navigationController
                })
                return navigationController
            }
            return bound
        }
    }

public val NavigationController.isInAndroidContext: Boolean
    get() = NavigationController.navigationControllerBindings.isNotEmpty()

internal val NavigationController.application: Application
    get() {
        return NavigationController.navigationControllerBindings.entries
            .firstOrNull {
                it.value == this
            }
            ?.key as? Application
            ?: throw EnroException.NavigationControllerIsNotAttached("NavigationController is not attached to an Application")
    }