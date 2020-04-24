package nav.enro.example

import android.app.Application
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.example.feature.*

class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        withComponent(masterDetailComponent)

        activityNavigator<LoginKey, LoginActivity> {
            defaultKey(LoginKey())
            acceptFragments(R.id.containerView, UserFragment::class)
        }

        fragmentNavigator<LoginErrorKey, LoginErrorFragment>()

        activityNavigator<DashboardKey, DashboardActivity>()
        activityNavigator<MultiStackKey, MultiStackActivity> {
            acceptFragments(R.id.redFrame, UserFragment::class)
            acceptFragments(R.id.greenFrame, UserFragment::class)
            acceptFragments(R.id.blueFrame, UserFragment::class)
        }
        activityNavigator<SearchKey, SearchActivity>()
        fragmentNavigator<DetailKey, DetailFragment>()
        fragmentNavigator<ListKey, ListFragment>()
        fragmentNavigator<UserKey, UserFragment>()
    }
}