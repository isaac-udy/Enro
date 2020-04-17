package nav.enro.core

import android.app.Application

class TestApplication : Application(), NavigationApplication {

    override val navigationController = NavigationController(
        navigators = listOf(
            activityNavigator<DefaultActivityKey, DefaultActivity> {
                defaultKey(defaultKey)
            },


            activityNavigator<GenericActivityKey, GenericActivity>(),
            fragmentNavigator<GenericFragmentKey, GenericFragment>(),


            activityNavigator<ActivityWithFragmentsKey, ActivityWithFragments> {
                defaultKey(ActivityWithFragmentsKey("default"))
                fragmentHost(android.R.id.content) {
                    listOf(ActivityChildFragment::class, ActivityChildFragmentTwo::class)
                        .contains(it.contextType)
                }
            },
            fragmentNavigator<ActivityChildFragmentKey, ActivityChildFragment>(),
            fragmentNavigator<ActivityChildFragmentTwoKey, ActivityChildFragmentTwo>()

        )
    )

    override fun onCreate() {
        super.onCreate()
        NavigationController.install(this)
    }
}