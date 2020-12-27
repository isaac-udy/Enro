package nav.enro

import android.app.Application
import nav.enro.core.activity.createActivityNavigator
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.core.fragment.createFragmentNavigator

class TestApplication : Application(), NavigationApplication {

    override val navigationController = navigationController {
        navigator(createActivityNavigator<DefaultActivityKey, DefaultActivity>())

        navigator(createActivityNavigator<GenericActivityKey, GenericActivity>())
        navigator(createFragmentNavigator<GenericFragmentKey, GenericFragment>())

        navigator(createActivityNavigator<ActivityWithFragmentsKey, ActivityWithFragments>())
        navigator(createFragmentNavigator<ActivityChildFragmentKey, ActivityChildFragment>())
        navigator(createFragmentNavigator<ActivityChildFragmentTwoKey, ActivityChildFragmentTwo>())
    }
}