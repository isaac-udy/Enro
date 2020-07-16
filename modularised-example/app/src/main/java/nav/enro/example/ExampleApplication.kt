package nav.enro.example

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import nav.enro.annotations.NavigationComponent
import nav.enro.core.context.activity
import nav.enro.core.controller.EnroLogger
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.core.executors.DefaultActivityExecutor
import nav.enro.example.core.data.UserRepository
import nav.enro.example.core.navigation.MultiStackKey
import nav.enro.example.core.navigation.UserKey
import nav.enro.example.dashboard.DashboardActivity
import nav.enro.example.login.LoginActivity
import nav.enro.example.login.LoginErrorDestination
import nav.enro.example.multistack.MultiStackActivity
import nav.enro.example.user.UserFragment
import nav.enro.result.EnroResult

@NavigationComponent
@HiltAndroidApp
class ExampleApplication : Application(), NavigationApplication {

    override val navigationController = navigationController {
        withPlugin(EnroResult())
        withPlugin(EnroLogger())

        override<MainActivity, LoginActivity>(
            launch = {
                DefaultActivityExecutor.open(it)
                it.fromContext.activity.overridePendingTransition(R.anim.fragment_fade_enter, R.anim.wait)
            },
            close = {
                DefaultActivityExecutor.close(it)
            }
        )

        override<MainActivity, DashboardActivity>(
            launch = {
                DefaultActivityExecutor.open(it)
                it.fromContext.activity.overridePendingTransition(R.anim.fragment_fade_enter, R.anim.wait)
            },
            close = {
                DefaultActivityExecutor.close(it)
            }
        )
    }

    override fun onCreate() {
        super.onCreate()
        UserRepository.initialise(this)
    }
}