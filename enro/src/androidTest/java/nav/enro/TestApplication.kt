package nav.enro

import android.app.Application
import nav.enro.annotations.NavigationComponent
import nav.enro.core.activity.createActivityNavigator
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.core.fragment.createFragmentNavigator
import nav.enro.core.plugins.EnroLogger
import nav.enro.result.EnroResult

@NavigationComponent
class TestApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        plugin(EnroLogger())
        plugin(EnroResult())
        plugin(TestPlugin)
    }
}