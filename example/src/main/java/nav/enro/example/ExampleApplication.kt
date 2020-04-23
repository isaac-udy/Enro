package nav.enro.example

import android.app.Application
import nav.enro.core.*
import nav.enro.core.internal.context.activity
import nav.enro.core.internal.context.requireActivity
import nav.enro.core.internal.executors.override.activityToActivityOverride
import nav.enro.core.internal.executors.override.activityToFragmentOverride
import nav.enro.core.internal.executors.override.fragmentToFragmentOverride
import nav.enro.core.internal.getAttributeResourceId
import nav.enro.example.feature.*

val override =
    activityToActivityOverride<LoginActivity, DashboardActivity>(
        launch = { it, _, intent ->
            it.activity.startActivity(intent)
            it.activity.overridePendingTransition(
                it.activity.getAttributeResourceId(android.R.attr.taskOpenEnterAnimation),
                it.activity.getAttributeResourceId(android.R.attr.taskOpenExitAnimation)
            )
        },
        close = {
            it.finish()
            it.overridePendingTransition(
                it.getAttributeResourceId(android.R.attr.taskCloseEnterAnimation),
                it.getAttributeResourceId(android.R.attr.taskCloseExitAnimation)
            )
        }
    )

val masterDetailOverride = activityToFragmentOverride<MasterDetailActivity, ListFragment>(
    launch = { activity, _, fragment ->
        activity.childFragmentManager.beginTransaction()
            .replace(R.id.master, fragment)
            .setPrimaryNavigationFragment(fragment)
            .commitNow()
    },
    close = { activity, _ ->
        activity.finish()
    }
)

val masterDetailOverride2 = activityToFragmentOverride<MasterDetailActivity, DetailFragment>(
    launch = { activity, _, fragment ->
        activity.childFragmentManager.beginTransaction()
            .replace(R.id.detail, fragment)
            .setPrimaryNavigationFragment(fragment)
            .commitNow()
    },
    close = { activity, fragment ->
        activity.supportFragmentManager.beginTransaction()
            .remove(fragment)
            .setPrimaryNavigationFragment(activity.supportFragmentManager.findFragmentById(R.id.master))
            .commitNow()
    }
)

class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = NavigationController(
        navigators = listOf(
            activityNavigator<LoginKey, LoginActivity>{
                defaultKey(LoginKey())

                fragmentHost(R.id.containerView) {
                    it == UserFragment::class
                }
            },
            fragmentNavigator<LoginErrorKey, LoginErrorFragment>(),

            activityNavigator<DashboardKey, DashboardActivity>(),
            activityNavigator<MultiStackKey, MultiStackActivity> {
                fragmentHost(R.id.redFrame) {
                    it == UserFragment::class
                }
                fragmentHost(R.id.greenFrame) {
                    it == UserFragment::class
                }
                fragmentHost(R.id.blueFrame) {
                    it == UserFragment::class
                }
            },
            activityNavigator<SearchKey, SearchActivity>(),
            fragmentNavigator<DetailKey, DetailFragment>(),
            fragmentNavigator<ListKey, ListFragment>(),

            activityNavigator<MasterDetailKey, MasterDetailActivity>(),
            fragmentNavigator<UserKey, UserFragment>()
        ),
        overrides = listOf(override, masterDetailOverride, masterDetailOverride2)
    )

    override fun onCreate() {
        super.onCreate()
        NavigationController.install(this)
    }
}