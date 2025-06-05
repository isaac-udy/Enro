@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import dev.enro.EnroController

object EnroTest {

    private var navigationController: EnroController? = null
    private var wasInstalled = false

    private val application: Any?
        get() {
            runCatching {
                return TODO("Application install support android")//ApplicationProvider.getApplicationContext()
            }
            return null
        }

    // TODO: Would be nice to add functionality to temporarily install a NavigationModule for a particular test
    fun installNavigationController() {
        if (navigationController != null) {
            uninstallNavigationController()
        }

        // Check if there's already an installed controller
        navigationController = EnroController.instance
        if (navigationController != null) {
            wasInstalled = true
            return
        }

        // Create a new controller for testing
        navigationController = EnroController().apply {
            install(application)
        }
        wasInstalled = false
    }

    fun uninstallNavigationController() {
        // Only uninstall if we created it
        if (!wasInstalled) {
            navigationController?.uninstall()
        }
        navigationController = null
    }

    fun getCurrentNavigationController(): EnroController {
        return navigationController ?: throw IllegalStateException("NavigationController is not installed")
    }

    fun disableAnimations(controller: EnroController) {
        // Animation control might need to be handled differently in the new API
        // For now, we'll leave this as a no-op
    }

    fun enableAnimations(controller: EnroController) {
        // Animation control might need to be handled differently in the new API
        // For now, we'll leave this as a no-op
    }
}
