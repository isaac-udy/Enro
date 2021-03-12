package dev.enro.core.controller

import android.app.Application
import nav.enro.core.controller.NavigationController

interface NavigationApplication {
    val navigationController: NavigationController
}

val Application.navigationController get() = (this as NavigationApplication).navigationController