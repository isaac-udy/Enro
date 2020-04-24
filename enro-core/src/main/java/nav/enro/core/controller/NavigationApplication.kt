package nav.enro.core.controller

import android.app.Application

interface NavigationApplication {
    val navigationController: NavigationController
}

val Application.navigationController get() = (this as NavigationApplication).navigationController