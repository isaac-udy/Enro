package dev.enro.example

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import nav.enro.annotations.NavigationComponent
import nav.enro.core.AnimationPair
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.core.plugins.EnroHilt
import nav.enro.core.plugins.EnroLogger
import nav.enro.example.core.data.UserRepository

@NavigationComponent
@HiltAndroidApp
class ExampleApplication : Application(), NavigationApplication {

    override val navigationController = navigationController {
        plugin(EnroHilt())
        plugin(EnroLogger())

        override<MainActivity, Any> {
            animation {
                AnimationPair.Resource(R.anim.fragment_fade_enter, R.anim.enro_no_op_animation)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        UserRepository.initialise(this)
    }
}