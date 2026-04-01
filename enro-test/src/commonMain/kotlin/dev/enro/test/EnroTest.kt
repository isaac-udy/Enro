@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import dev.enro.EnroController
import dev.enro.controller.NavigationModule

object EnroTest {

    private var navigationController: EnroController? = null
    private var wasInstalled = false
    private val installedModules: MutableList<NavigationModule> = mutableListOf()

    private val application: Any?
        get() = getTestApplicationContext()

    fun installModule(module: NavigationModule) {
        val controller = navigationController
            ?: throw IllegalStateException("NavigationController is not installed, call installNavigationController() before installModule()")
        controller.addModule(module)
        installedModules.add(module)
    }

    fun removeModule(module: NavigationModule) {
        val controller = navigationController
            ?: throw IllegalStateException("NavigationController is not installed")
        controller.removeModule(module)
        installedModules.remove(module)
    }

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
        // Remove any temporarily installed modules before uninstalling
        navigationController?.let { controller ->
            installedModules.forEach { controller.removeModule(it) }
        }
        installedModules.clear()

        // Only uninstall if we created it
        if (!wasInstalled) {
            navigationController?.uninstall()
        }
        navigationController = null

        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        dev.enro.viewmodel.NavigationHandleProvider.clearAllForTest()
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
