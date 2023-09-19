package dev.enro

import android.app.Application
import dev.enro.annotations.NavigationComponent
import dev.enro.destination.compose.composableDestination
import dev.enro.android.NavigationApplication
import dev.enro.core.controller.createNavigationController
import dev.enro.core.destinations.ComposableDestinations
import dev.enro.core.destinations.ManuallyBoundComposableScreen
import dev.enro.core.plugins.EnroLogger
import dev.enro.test.EnroTest
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
        LeakCanary.config = LeakCanary.config.copy(
            referenceMatchers = AndroidReferenceMatchers.appDefaults +
                    AndroidReferenceMatchers.instanceFieldLeak(
                        className = "androidx.activity.ComponentActivity\$ReportFullyDrawnExecutorApi16Impl",
                        fieldName = "this\$0",
                        description = "The ComponentActivity's ReportFullyFullyDrawnExecutorAPI16Impl can sometimes " +
                                "leak with ActivityScenarios if they are recreated quickly and contain composables. " +
                                "To reproduce a leak that shows this, add a setContentView { ... } to EmptyActivity, " +
                                "launch EmptyActivity as an ActivityScenario, and then recreate it. Particularly on API 27 " +
                                "this will cause a DetectLeaksAfterTestSuccess to fail the test",
                    )
        )
    }
}

