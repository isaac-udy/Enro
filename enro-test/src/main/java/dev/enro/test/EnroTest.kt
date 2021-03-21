package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationController

object EnroTest {
    fun getCurrentNavigationController(): NavigationController {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val navigationApplication = application as? NavigationApplication
            ?: throw IllegalStateException("The Application instance for the current test ($application) is not a NavigationApplication")

        return navigationApplication.navigationController
    }
}