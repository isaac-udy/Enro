package dev.enro.tests.application

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import dev.enro.tests.application.TestActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CoreTestSuite {
    @Test
    fun applicationLaunches() {
        // Context of the app under test. These tests now run as the self-instrumenting
        // androidDeviceTest of the :tests:application:common library, so the target
        // context's package is "dev.enro.tests.application.test" rather than the
        // "dev.enro.tests.application" of the former application module. Strip the
        // instrumentation ".test" suffix so the assertion holds in both setups.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("dev.enro.tests.application", appContext.packageName.removeSuffix(".test"))
    }

    @Test
    fun activityLaunches() {
        ActivityScenario.launch(TestActivity::class.java)
        Thread.sleep(1000)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            val activity = activities.firstOrNull { it is TestActivity }
            assertNotNull(activity)
        }
    }
}