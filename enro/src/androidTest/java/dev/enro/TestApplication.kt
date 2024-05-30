package dev.enro

import android.app.Application
import android.os.Build
import dev.enro.annotations.NavigationComponent
import dev.enro.core.compose.composableDestination
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.createNavigationController
import dev.enro.core.destinations.ComposableDestinations
import dev.enro.core.destinations.ManuallyBoundComposableScreen
import dev.enro.core.plugins.EnroLogger
import dev.enro.test.EnroTest
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import shark.AndroidReferenceMatchers

@NavigationComponent
open class TestApplication : Application(), NavigationApplication {
    override val navigationController = createNavigationController {
        plugin(EnroLogger())
        plugin(TestPlugin)

        composableDestination<ComposableDestinations.ManuallyBound> { ManuallyBoundComposableScreen() }
    }.also { EnroTest.disableAnimations(it) }

    override fun onCreate() {
        super.onCreate()

        // Ignoring library leak, see here: https://issuetracker.google.com/issues/277434271
        val referenceMatchers = AndroidReferenceMatchers.appDefaults.toMutableList()
        referenceMatchers += AndroidReferenceMatchers.instanceFieldLeak(
            className = "androidx.activity.ComponentActivity\$ReportFullyDrawnExecutorApi16Impl",
            fieldName = "this\$0",
            description = "The ComponentActivity's ReportFullyFullyDrawnExecutorAPI16Impl can sometimes " +
                    "leak with ActivityScenarios if they are recreated quickly and contain composables. " +
                    "To reproduce a leak that shows this, add a setContentView { ... } to EmptyActivity, " +
                    "launch EmptyActivity as an ActivityScenario, and then recreate it. Particularly on API 27 " +
                    "this will cause a DetectLeaksAfterTestSuccess to fail the test",
        )
        if (Build.VERSION.SDK_INT == 23) {
            referenceMatchers += AndroidReferenceMatchers.instanceFieldLeak(
                className = "dev.enro.core.hosts.AbstractFragmentHostForPresentableFragment\$\$ExternalSyntheticLambda1",
                fieldName = "f\$0",
                description = "This appears to be a flaky leak for tests running in API 23, but which can't be reproduced outside of CI",
            )
            referenceMatchers += AndroidReferenceMatchers.instanceFieldLeak(
                className = "dev.enro.core.hosts.AbstractFragmentHostForPresentableFragment\$\$ExternalSyntheticLambda1",
                fieldName = "f\$1",
                description = "This appears to be a flaky leak for tests running in API 23, but which can't be reproduced outside of CI",
            )
        }
        // Temporarily remove app watchers for SDK versions less than 33, due to bug with androidx viewmodel
        // https://issuetracker.google.com/issues/341792251
        // https://github.com/square/leakcanary/issues/2677
        if (Build.VERSION.SDK_INT <= 33) {
            AppWatcher.manualInstall(
                application = this,
                watchersToInstall = emptyList()
            )
        }
        else {
            AppWatcher.manualInstall(this)
        }

        LeakCanary.config = LeakCanary.config.copy(referenceMatchers = referenceMatchers)
    }
}

