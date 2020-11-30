package nav.enro.example

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import nav.enro.annotations.NavigationComponent
import nav.enro.core.NavigationApplication
import nav.enro.core.navigationController
import nav.enro.core.activity.DefaultActivityExecutor
import nav.enro.core.plugins.EnroHilt
import nav.enro.core.plugins.EnroLogger
import nav.enro.example.core.data.UserRepository
import nav.enro.example.dashboard.DashboardActivity
import nav.enro.example.login.LoginActivity
import nav.enro.result.EnroResult

@NavigationComponent
@HiltAndroidApp
class ExampleApplication : Application(), NavigationApplication {

    override val navigationController = navigationController {
        withPlugin(EnroHilt())
        withPlugin(EnroResult())
        withPlugin(EnroLogger())

        override<MainActivity, LoginActivity>(
            launch = {
                DefaultActivityExecutor.open(it)
                it.fromContext.activity.overridePendingTransition(R.anim.fragment_fade_enter, R.anim.enro_no_op_animation)
            },
            close = {
                DefaultActivityExecutor.close(it)
            }
        )

        override<MainActivity, DashboardActivity>(
            launch = {
                DefaultActivityExecutor.open(it)
                it.fromContext.activity.overridePendingTransition(R.anim.fragment_fade_enter, R.anim.enro_no_op_animation)
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