package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationComponentBuilder
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroLogger

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
            .callPrivate<NavigationController>("build")
            .apply {
                isInTest = true
            }

        if (isInstrumented()) {
            val application = ApplicationProvider.getApplicationContext<Application>()
            if (application is NavigationApplication) {
                navigationController = application.navigationController
                return
            }
            navigationController?.apply { install(application) }
        } else {
            navigationController?.callPrivate<Unit>("installForJvmTests")
        }
    }

    fun uninstallNavigationController() {
        val providerClass =
            Class.forName("dev.enro.viewmodel.EnroViewModelNavigationHandleProvider")
        val instance = providerClass.getDeclaredField("INSTANCE").get(null)!!
        instance.callPrivate<Unit>("clearAllForTest")
        navigationController?.apply {
            isInTest = false
        }

        val uninstallNavigationController = navigationController
        navigationController = null

        if (isInstrumented()) {
            val application = ApplicationProvider.getApplicationContext<Application>()
            if (application is NavigationApplication) return
            uninstallNavigationController?.callPrivate<Unit>("uninstall", application)
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


private fun <T> Any.callPrivate(methodName: String, vararg args: Any): T {
    val method = this::class.java.declaredMethods.filter { it.name.startsWith(methodName) }.first()
    method.isAccessible = true
    val result = method.invoke(this, *args)
    method.isAccessible = false
    return result as T
}


private var NavigationController.isInTest: Boolean
    get() {
        return NavigationController::class.java.getDeclaredField("isInTest")
            .let {
                it.isAccessible = true
                val result = it.get(this) as Boolean
                it.isAccessible = false

                return@let result
            }
    }
    set(value) {
        NavigationController::class.java.getDeclaredField("isInTest")
            .let {
                it.isAccessible = true
                val result = it.set(this, value)
                it.isAccessible = false

                return@let result
            }
    }