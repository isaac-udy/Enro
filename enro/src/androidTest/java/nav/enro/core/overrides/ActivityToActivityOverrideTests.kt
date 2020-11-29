package nav.enro.core.overrides

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import junit.framework.Assert.assertTrue
import nav.enro.*
import nav.enro.core.*
import nav.enro.core.controller.navigationController
import nav.enro.core.executors.createOverride
import nav.enro.core.executors.defaultClose
import nav.enro.core.executors.defaultLaunch
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
        ActivityScenario.launch(DefaultActivity::class.java)
            .getNavigationHandle<DefaultActivityKey>()
            .forward(GenericActivityKey("override test"))

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

        ActivityScenario.launch(DefaultActivity::class.java)
            .getNavigationHandle<DefaultActivityKey>()
            .forward(GenericActivityKey("override test"))

        expectActivity<GenericActivity>()
            .getNavigationHandle()
            .close()

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

        ActivityScenario.launch<GenericActivity>(intent)
            .getNavigationHandle<GenericActivityKey>()
            .forward(GenericActivityKey("override test 2"))

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

        ActivityScenario.launch<GenericActivity>(intent)
            .getNavigationHandle<GenericActivityKey>()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity>()
            .getNavigationHandle()
            .close()

        expectActivity<GenericActivity>()

        assertTrue(closeOverrideCalled)
    }


    @Test
    fun givenUnboundActivityToActivityOverride_whenActivityIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<UnboundActivity, GenericActivity>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<GenericActivity>().invoke(it)
                }
            )
        )

        ActivityScenario.launch(UnboundActivity::class.java)
        expectActivity<UnboundActivity>().getNavigationHandle()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenUnboundActivityToActivityOverride_whenActivityIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<UnboundActivity, GenericActivity>(
                close = {
                    closeOverrideCalled = true
                    defaultClose<GenericActivity>().invoke(it)
                }
            )
        )

        ActivityScenario.launch(UnboundActivity::class.java)
        expectActivity<UnboundActivity>().getNavigationHandle()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity>()
            .getNavigationHandle()
            .close()

        expectActivity<UnboundActivity>()

        assertTrue(closeOverrideCalled)
    }
}