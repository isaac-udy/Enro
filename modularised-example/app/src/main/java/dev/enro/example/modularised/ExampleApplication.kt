package dev.enro.example.modularised

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.enro.annotations.NavigationComponent
import dev.enro.core.AnimationPair
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.navigationController
import dev.enro.core.plugins.EnroHilt
import dev.enro.core.plugins.EnroLogger
import dev.enro.example.core.data.UserRepository

@NavigationComponent
@HiltAndroidApp
class ExampleApplication : Application(), NavigationApplication {

    override val navigationController = navigationController {
        plugin(EnroHilt())
        plugin(EnroLogger())

        override<MainActivity, Any> {
            animation {
                AnimationPair.Resource(android.R.anim.fade_in, R.anim.enro_no_op_animation)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        UserRepository.initialise(this)
    }
}