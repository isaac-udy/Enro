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

class ActivityToFragmentOverrideTests() {

    @Test
    fun givenActivityToFragmentOverride_andActivityDoesNotSupportFragment_whenInitialActivityOpenedWithDefaultKey_whenFragmentIsLaunched_whenActivityDoes_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<DefaultActivity, GenericFragment>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<GenericFragment>().invoke(it)
                }
            )
        )
        ActivityScenario.launch(DefaultActivity::class.java)
            .getNavigationHandle<DefaultActivityKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_andActivityDoesNotSupportFragment_whenInitialActivityOpenedWithDefaultKey_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<DefaultActivity, GenericFragment>(
                close = {
                    closeOverrideCalled = true
                    defaultClose<GenericFragment>().invoke(it)
                }
            )
        )

        ActivityScenario.launch(DefaultActivity::class.java)
            .getNavigationHandle<DefaultActivityKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle().close()

        expectActivity<DefaultActivity>()

        assertTrue(closeOverrideCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_andActivityDoesNotSupportFragment_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericActivity, GenericFragment>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<GenericFragment>().invoke(it)
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
            .forward(GenericFragmentKey("override test 2"))

        expectFragment<GenericFragment>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_andActivityDoesNotSupportFragment_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericActivity, GenericFragment>(
                close = {
                    closeOverrideCalled = true
                    defaultClose<GenericFragment>().invoke(it)
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
            .forward(GenericFragmentKey("override test 2"))

        expectFragment<GenericFragment>()
            .getNavigationHandle()
            .close()

        expectActivity<GenericActivity>()

        assertTrue(closeOverrideCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityWithFragments, ActivityChildFragment>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<ActivityChildFragment>().invoke(it)
                }
            )
        )
        val intent = Intent(application, ActivityWithFragments::class.java)
            .addOpenInstruction(
                NavigationInstruction.Open(
                    NavigationDirection.FORWARD,
                    ActivityWithFragmentsKey(id = "override test")
                )
            )

        ActivityScenario.launch<ActivityWithFragments>(intent)
            .getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test 2"))

        expectFragment<ActivityChildFragment>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityWithFragments, ActivityChildFragment>(
                close = {
                    closeOverrideCalled = true
                    defaultClose<ActivityChildFragment>().invoke(it)
                }
            )
        )
        val intent = Intent(application, ActivityWithFragments::class.java)
            .addOpenInstruction(
                NavigationInstruction.Open(
                    NavigationDirection.FORWARD,
                    ActivityWithFragmentsKey(id = "override test")
                )
            )

        ActivityScenario.launch<ActivityWithFragments>(intent)
            .getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test 2"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .close()

        expectActivity<ActivityWithFragments>()

        assertTrue(closeOverrideCalled)
    }
}