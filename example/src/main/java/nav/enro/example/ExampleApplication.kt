package nav.enro.example

import android.app.Application
import nav.enro.core.NavigationKey
import nav.enro.core.controller.EnroLogger
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.example.feature.*
import nav.enro.masterdetail.masterDetailComponent
import nav.enro.multistack.MultiStackContainer
import nav.enro.multistack.multiStackComponent
import nav.enro.result.EnroResult
import kotlin.reflect.KClass

class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        withPlugin(EnroResult())
        withPlugin(EnroLogger())

        activityNavigator<MasterDetailKey, MasterDetailActivity>()
        withComponent(
            masterDetailComponent<MasterDetailActivity, ListFragment, DetailFragment>(
                masterContainer = R.id.master,
                detailContainer = R.id.detail
            )
        )

        activityNavigator<LoginKey, LoginActivity> {
            defaultKey(LoginKey())
            acceptFragments(R.id.containerView, UserFragment::class)
        }

        fragmentNavigator<LoginErrorKey, LoginErrorFragment>()

        activityNavigator<DashboardKey, DashboardActivity>()

        withComponent(multiStackComponent<MultiStackActivity>(
            MultiStackContainer(R.id.redFrame, UserKey("Red")),
            MultiStackContainer(R.id.greenFrame, UserKey("Green")),
            MultiStackContainer(R.id.blueFrame, UserKey("Blue"))
        ))
        activityNavigator<MultiStackKey, MultiStackActivity>() {
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