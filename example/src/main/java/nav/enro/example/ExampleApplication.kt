package nav.enro.example

import android.app.Application
import nav.enro.core.controller.EnroLogger
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.example.feature.*
import nav.enro.result.EnroResult

class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        withComponent(masterDetailComponent)
        withPlugin(EnroResult())
        withPlugin(EnroLogger())

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