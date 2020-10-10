package nav.enro.core.overrides

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import junit.framework.Assert.assertTrue
import nav.enro.core.*
import nav.enro.core.controller.navigationController
import nav.enro.core.executors.override.createOverride
import nav.enro.core.executors.override.defaultLaunch
import nav.enro.core.expectFragment
import org.junit.Before
import org.junit.Test

class FragmentToFragmentOverrideTests() {

    lateinit var initialScenario: ActivityScenario<ActivityWithFragments>

    @Before
    fun before() {
        val intent = Intent(application, ActivityWithFragments::class.java)
            .addOpenInstruction(
                NavigationInstruction.Open(
                    NavigationDirection.FORWARD,
                    ActivityWithFragmentsKey(id = "initial activity")
                )
            )

        initialScenario = ActivityScenario.launch<ActivityWithFragments>(intent)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsStandalone_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericFragment, ActivityChildFragment>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<ActivityChildFragment>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle<GenericFragmentKey>()
            .forward(ActivityChildFragmentKey("override test 2"))

        expectFragment<ActivityChildFragment>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsStandalone_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericFragment, ActivityChildFragment>(
                launch = {
                    closeOverrideCalled = true
                    defaultLaunch<ActivityChildFragment>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle<GenericFragmentKey>()
            .forward(ActivityChildFragmentKey("override test 2"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle<ActivityChildFragmentKey>()
            .close()

        expectFragment<GenericFragment>()

        assertTrue(closeOverrideCalled)
    }


    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsStandalone_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, GenericFragment>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<GenericFragment>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle<ActivityChildFragmentKey>()
            .forward(GenericFragmentKey("override test 2"))

        expectFragment<GenericFragment>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsStandalone_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, GenericFragment>(
                launch = {
                    closeOverrideCalled = true
                    defaultLaunch<GenericFragment>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle<ActivityChildFragmentKey>()
            .forward(GenericFragmentKey("override test 2"))

        expectFragment<GenericFragment>()
            .getNavigationHandle<GenericFragmentKey>()
            .close()

        expectFragment<ActivityChildFragment>()

        assertTrue(closeOverrideCalled)
    }


    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsNested_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, ActivityChildFragmentTwo>(
                launch = {
                    launchOverrideCalled = true
                    defaultLaunch<ActivityChildFragmentTwo>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle<ActivityChildFragmentKey>()
            .forward(ActivityChildFragmentTwoKey("override test 2"))

        expectFragment<ActivityChildFragmentTwo>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsNested_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, ActivityChildFragmentTwo>(
                launch = {
                    closeOverrideCalled = true
                    defaultLaunch<ActivityChildFragmentTwo>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle<ActivityChildFragmentKey>()
            .forward(ActivityChildFragmentTwoKey("override test 2"))

        expectFragment<ActivityChildFragmentTwo>()
            .getNavigationHandle<ActivityChildFragmentTwoKey>()
            .close()

        expectFragment<ActivityChildFragment>()

        assertTrue(closeOverrideCalled)
    }

}