package nav.enro.core.overrides

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import junit.framework.Assert.assertTrue
import nav.enro.*
import nav.enro.core.*
import nav.enro.core.navigationController
import nav.enro.core.createOverride
import nav.enro.core.defaultOpen
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
                open = {
                    launchOverrideCalled = true
                    defaultOpen<ActivityChildFragment>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle()
            .forward(ActivityChildFragmentKey("override test 2"))

        expectFragment<ActivityChildFragment>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsStandalone_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericFragment, ActivityChildFragment>(
                open = {
                    closeOverrideCalled = true
                    defaultOpen<ActivityChildFragment>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle()
            .forward(ActivityChildFragmentKey("override test 2"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .close()

        expectFragment<GenericFragment>()

        assertTrue(closeOverrideCalled)
    }


    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsStandalone_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, GenericFragment>(
                open = {
                    launchOverrideCalled = true
                    defaultOpen<GenericFragment>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .forward(GenericFragmentKey("override test 2"))

        expectFragment<GenericFragment>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsStandalone_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, GenericFragment>(
                open = {
                    closeOverrideCalled = true
                    defaultOpen<GenericFragment>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .forward(GenericFragmentKey("override test 2"))

        expectFragment<GenericFragment>()
            .getNavigationHandle()
            .close()

        expectFragment<ActivityChildFragment>()

        assertTrue(closeOverrideCalled)
    }


    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsNested_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, ActivityChildFragmentTwo>(
                open = {
                    launchOverrideCalled = true
                    defaultOpen<ActivityChildFragmentTwo>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .forward(ActivityChildFragmentTwoKey("override test 2"))

        expectFragment<ActivityChildFragmentTwo>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsNested_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, ActivityChildFragmentTwo>(
                open = {
                    closeOverrideCalled = true
                    defaultOpen<ActivityChildFragmentTwo>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .forward(ActivityChildFragmentTwoKey("override test 2"))

        expectFragment<ActivityChildFragmentTwo>()
            .getNavigationHandle()
            .close()

        expectFragment<ActivityChildFragment>()

        assertTrue(closeOverrideCalled)
    }

}