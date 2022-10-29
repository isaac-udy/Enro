@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationComponentBuilder
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroLogger
import dev.enro.viewmodel.EnroViewModelNavigationHandleProvider

object EnroTest {

    private var navigationController: NavigationController? = null

    fun installNavigationController() {
        if (navigationController != null) {
            uninstallNavigationController()
        }
        navigationController = NavigationComponentBuilder()
            .apply {
                plugin(EnroLogger())
            }
            .build()
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

    private fun isInstrumented(): Boolean {
        runCatching {
            InstrumentationRegistry.getInstrumentation()
            return true
        }
        return false
    }
}