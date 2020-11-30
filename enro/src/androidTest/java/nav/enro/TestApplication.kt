package nav.enro

import android.app.Application
import nav.enro.core.NavigationApplication
import nav.enro.core.activity.createActivityNavigator
import nav.enro.core.controller.NavigationController
import nav.enro.core.fragment.createFragmentNavigator

class TestApplication : Application(),
    NavigationApplication {

    override val navigationController =
        NavigationController(
            navigators = listOf(
                createActivityNavigator<DefaultActivityKey, DefaultActivity>(),

                createActivityNavigator<GenericActivityKey, GenericActivity>(),
                createFragmentNavigator<GenericFragmentKey, GenericFragment>(),

                createActivityNavigator<ActivityWithFragmentsKey, ActivityWithFragments>(),
                createFragmentNavigator<ActivityChildFragmentKey, ActivityChildFragment>(),
                createFragmentNavigator<ActivityChildFragmentTwoKey, ActivityChildFragmentTwo>()
            )
        )

    override fun onCreate() {
        super.onCreate()
        NavigationController.install(this)
    }
}