@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.createUnattachedNavigationController
import dev.enro.core.internal.EnroLog
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
            setConfig(
                config.copy(
                    isInTest = true
                )
            )
            when (application) {
                null -> EnroLog.usePrint = true
                else -> EnroLog.usePrint = false
            }
            when (val application = application) {
                is NavigationApplication -> return@apply
                null -> installForJvmTests()
                else -> installForAny(application)
            }
        }
    }

    fun uninstallNavigationController() {
        EnroViewModelNavigationHandleProvider.clearAllForTest()
        TestNavigationHandle.allInstructions.clear()
        navigationController?.apply {
            setConfig(
                config.copy(
                    isInTest = false
                )
            )
        }

        navigationController?.apply {
            setConfig(
                config.copy(
                    isInTest = false
                )
            )
            if (application is NavigationApplication) return@apply
            uninstall(application ?: return@apply)
        }
        navigationController = null
    }

    fun getCurrentNavigationController(): NavigationController {
        return navigationController!!
    }

    fun disableAnimations(controller: NavigationController) {
        controller.setConfig(
            controller.config.copy(
                isAnimationsDisabled = true
            )
        )
    }

    fun enableAnimations(controller: NavigationController) {
        controller.setConfig(
            controller.config.copy(
                isAnimationsDisabled = false
            )
        )
    }
}

