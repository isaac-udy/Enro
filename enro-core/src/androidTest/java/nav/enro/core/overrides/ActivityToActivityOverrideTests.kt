package nav.enro.core.overrides

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import junit.framework.Assert.assertTrue
import nav.enro.core.*
import nav.enro.core.controller.navigationController
import nav.enro.core.executors.override.createOverride
import nav.enro.core.executors.override.defaultClose
import nav.enro.core.executors.override.defaultLaunch
import org.junit.Test

class ActivityToActivityOverrideTests() {

    @Test
    fun givenActivityToActivityOverride_whenInitialActivityOpenedWithDefaultKey_whenActivityIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<DefaultActivity, GenericActivity>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<GenericActivity>().invoke(it)
                }
            )
        )
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()
        handle.forward(GenericActivityKey("override test"))

        expectActivity<GenericActivity>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenActivityToActivityOverride_whenInitialActivityOpenedWithDefaultKey_whenActivityIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<DefaultActivity, GenericActivity>(
                close = {
                    closeOverrideCalled = true
                    defaultClose<GenericActivity>().invoke(it)
                }
            )
        )
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        handle.forward(GenericActivityKey("override test"))
        val genericActivity = expectActivity<GenericActivity>()

        genericActivity.getNavigationHandle<GenericActivityKey>().close()
        expectActivity<DefaultActivity>()

        assertTrue(closeOverrideCalled)
    }

    @Test
    fun givenActivityToActivityOverride_whenActivityIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericActivity, GenericActivity>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<GenericActivity>().invoke(it)
                }
            )
        )
        val intent = Intent(application, GenericActivity::class.java)
            .addOpenInstruction(
                NavigationInstruction.Open(
                    NavigationDirection.FORWARD,
                    GenericActivityKey(id = "override test")
                )
            )

        val scenario = ActivityScenario.launch<GenericActivity>(intent)
        val handle = scenario.getNavigationHandle<GenericActivityKey>()

        handle.forward(GenericActivityKey("override test 2"))
        expectActivity<GenericActivity>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenActivityToActivityOverride_whenActivityIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericActivity, GenericActivity>(
                close = {
                    closeOverrideCalled = true
                    defaultClose<GenericActivity>().invoke(it)
                }
            )
        )

        val intent = Intent(application, GenericActivity::class.java)
            .addOpenInstruction(
                NavigationInstruction.Open(
                    NavigationDirection.FORWARD,
                    GenericActivityKey(id = "override test")
                )
            )

        val scenario = ActivityScenario.launch<GenericActivity>(intent)
        val handle = scenario.getNavigationHandle<GenericActivityKey>()

        handle.forward(GenericActivityKey("override test 2"))
        val genericActivity = expectActivity<GenericActivity>()

        genericActivity.getNavigationHandle<GenericActivityKey>().close()
        expectActivity<GenericActivity>()

        assertTrue(closeOverrideCalled)
    }

}