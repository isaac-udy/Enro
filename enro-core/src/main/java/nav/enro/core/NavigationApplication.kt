package nav.enro.core

import android.app.Application

interface NavigationApplication {
    val navigationController: NavigationController
}

internal val Application.navigationController get() = (this as NavigationApplication).navigationController