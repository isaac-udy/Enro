package nav.enro.core

import android.R
import android.app.Application
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.NavigationController
import nav.enro.core.navigator.createActivityNavigator
import nav.enro.core.navigator.createFragmentNavigator

class TestApplication : Application(),
    NavigationApplication {

    override val navigationController =
        NavigationController(
            navigators = listOf(
                createActivityNavigator<DefaultActivityKey, DefaultActivity> {
                    defaultKey(defaultKey)
                },

                createActivityNavigator<GenericActivityKey, GenericActivity>(),
                createFragmentNavigator<GenericFragmentKey, GenericFragment>(),

                createActivityNavigator<ActivityWithFragmentsKey, ActivityWithFragments> {
                    defaultKey(ActivityWithFragmentsKey("default"))
                },
                createFragmentNavigator<ActivityChildFragmentKey, ActivityChildFragment>(),
                createFragmentNavigator<ActivityChildFragmentTwoKey, ActivityChildFragmentTwo>()

            )
        )

    override fun onCreate() {
        super.onCreate()
        NavigationController.install(this)
    }
}