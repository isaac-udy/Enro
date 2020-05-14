package nav.enro.example

import android.app.Application
import nav.enro.annotations.NavigationComponent
import nav.enro.core.controller.EnroLogger
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.example.core.navigation.MultiStackKey
import nav.enro.example.core.navigation.UserKey
import nav.enro.example.multistack.MultiStackActivity
import nav.enro.example.user.UserFragment
import nav.enro.result.EnroResult

@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {

    override val navigationController = navigationController {
        withPlugin(EnroResult())
        withPlugin(EnroLogger())
    }
}