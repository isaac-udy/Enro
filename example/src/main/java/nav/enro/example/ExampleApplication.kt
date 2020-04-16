package nav.enro.example

import android.app.Application
import nav.enro.core.NavigationApplication
import nav.enro.core.NavigationController
import nav.enro.core.activityNavigator
import nav.enro.core.fragmentNavigator
import nav.enro.example.feature.*

class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = NavigationController(
        navigators = listOf(
            activityNavigator<LoginKey, LoginActivity>{
                defaultKey(LoginKey())

                fragmentHost(R.id.containerView) {
                    it.contextType == UserFragment::class
                }
            },
            fragmentNavigator<LoginErrorKey, LoginErrorFragment>(),

            activityNavigator<DashboardKey, DashboardActivity>(),
            activityNavigator<SearchKey, SearchActivity>(),
            activityNavigator<DetailKey, DetailActivity>(),
            activityNavigator<ListKey, ListActivity>(),
            fragmentNavigator<UserKey, UserFragment>()
        )
    )

    override fun onCreate() {
        super.onCreate()
        NavigationController.install(this)
    }
}