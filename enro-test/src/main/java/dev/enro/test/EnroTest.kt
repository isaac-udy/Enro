package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationComponentBuilder
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroLogger

object EnroTest {
    internal fun installNavigationController() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        if(application is NavigationApplication) return

        NavigationComponentBuilder()
            .apply {
                plugin(EnroLogger())
            }
            .callPrivate<NavigationController>("build")
            .apply { install(application) }
    }

    internal fun uninstallNavigationController() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        if(application is NavigationApplication) return
        getCurrentNavigationController().callPrivate<Unit>("uninstall", application)
    }

    fun getCurrentNavigationController(): NavigationController {
        val application = ApplicationProvider.getApplicationContext<Application>()
        if(application is NavigationApplication) return application.navigationController
        return NavigationController.callPrivate("getBoundApplicationForTest", application)
    }
}

private fun <T> Any.callPrivate(methodName: String, vararg args: Any): T {
    val method = this::class.java.declaredMethods.filter { it.name.startsWith(methodName) }.first()
    method.isAccessible = true
    val result = method.invoke(this, *args)
    method.isAccessible = false
    return result as T
}