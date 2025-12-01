package dev.enro.tests.application

import dev.enro.installNavigationController
import platform.UIKit.UIApplication

object TestApplication {
    fun install(application: UIApplication) {
        installNavigationController(application)
    }
}