@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.createUnattachedNavigationController
import dev.enro.viewmodel.EnroViewModelNavigationHandleProvider

object EnroTest {

    private var navigationController: NavigationController? = null

    private val application: Application?
        get() {
            runCatching {
                return ApplicationProvider.getApplicationContext()
            }
            return null
        }

    fun installNavigationController() {
        if (navigationController != null) {
            uninstallNavigationController()
        }

        navigationController = when (val application = application) {
            is NavigationApplication -> application.navigationController
            else -> createUnattachedNavigationController()
        }.apply {
            isInTest = true
            when (val application = application) {
                is NavigationApplication -> return@apply
                null -> installForJvmTests()
                else -> install(application)
            }
        }
    }

    fun uninstallNavigationController() {
        EnroViewModelNavigationHandleProvider.clearAllForTest()
        navigationController?.apply {
            isInTest = false
        }

        navigationController?.apply {
            isInTest = false
            if (application is NavigationApplication) return@apply
            uninstall(application ?: return@apply)
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
}

