package dev.enro

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.CustomTestApplication
import dev.enro.core.controller.createNavigationModule
import dev.enro.core.controller.navigationController

@CustomTestApplication(TestApplication::class)
interface HiltTestApplication

class HiltTestApplicationRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication_Application::class.java.name, context).apply {
            navigationController.addModule(createNavigationModule {
                TestApplicationNavigation().execute(this)
            })
        }
    }

}

