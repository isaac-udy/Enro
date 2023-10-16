@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.createUnattachedNavigationController
import dev.enro.viewmodel.EnroViewModelNavigationHandleProvider

object EnroTest {

    private var navigationController: NavigationController? = null

    fun installNavigationController() {
        if (navigationController != null) {
            uninstallNavigationController()
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
            navigationController = createUnattachedNavigationController()
                .apply {
                    isInTest = true
                    installForJvmTests()
                }
        }
    }

    fun uninstallNavigationController() {
        EnroViewModelNavigationHandleProvider.clearAllForTest()
        navigationController?.apply {
            isInTest = false
        }
        navigationController = null
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

