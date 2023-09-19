@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.android.NavigationApplication
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.createUnattachedNavigationController
import dev.enro.android.viewmodel.EnroViewModelNavigationHandleProvider

object EnroTest {

    private var navigationController: NavigationController? = null

    fun installNavigationController() {
        if (navigationController != null) {
            uninstallNavigationController()
        }
        navigationController = createUnattachedNavigationController()
            .apply {
                isInTest = true
            }

        if (isInstrumented()) {
            val application = ApplicationProvider.getApplicationContext<Application>()
            if (application is NavigationApplication) {
                navigationController = application.navigationController.apply {
                    isInTest = true
                }
                return
            }
            navigationController?.apply { install(application) }
        } else {
            navigationController?.installForJvmTests()
        }
    }

    fun uninstallNavigationController() {
        EnroViewModelNavigationHandleProvider.clearAllForTest()
        navigationController?.apply {
            isInTest = false
        }

        val uninstallNavigationController = navigationController
        navigationController = null

        if (isInstrumented()) {
            val application = ApplicationProvider.getApplicationContext<Application>()
            if (application is NavigationApplication) return
            uninstallNavigationController?.uninstall(application)
        }
    }

    fun getCurrentNavigationController(): NavigationController {
        return navigationController!!
    }

    fun disableAnimations(controller: NavigationController) {
        controller.isAnimationsDisabled = true
    }

    fun enableAnimations(controller: NavigationController) {
        controller.isAnimationsDisabled = false
    }

    private fun isInstrumented(): Boolean {
        runCatching {
            InstrumentationRegistry.getInstrumentation()
            return true
        }
        return false
    }
}