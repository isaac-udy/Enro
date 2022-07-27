package dev.enro

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.CustomTestApplication
import dev.enro.annotations.NavigationComponent
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.createNavigationComponent
import dev.enro.core.controller.navigationController
import dev.enro.core.plugins.EnroLogger

@NavigationComponent
open class TestApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        plugin(EnroLogger())
        plugin(TestPlugin)
    }
}
@CustomTestApplication(TestApplication::class)
interface HiltTestApplication

class HiltTestApplicationRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication_Application::class.java.name, context).apply {
            navigationController.addComponent(createNavigationComponent {
                TestApplicationNavigation().execute(this)
            })
        }
    }
}