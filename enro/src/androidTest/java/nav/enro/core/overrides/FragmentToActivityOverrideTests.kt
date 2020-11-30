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

class FragmentToActivityOverrideTests() {

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
    fun givenFragmentToActivityOverride_whenFragmentIsStandalone_whenActivityIsLaunchedFrom_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericFragment, GenericActivity>(
                open = {
                    launchOverrideCalled = true
                    defaultOpen<GenericActivity>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenFragmentToActivityOverride_whenFragmentIsStandalone_whenActivityIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<GenericFragment, GenericActivity>(
                open = {
                    closeOverrideCalled = true
                    defaultOpen<GenericActivity>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity>()
            .getNavigationHandle()
            .close()

        expectFragment<GenericFragment>()

        assertTrue(closeOverrideCalled)
    }


    @Test
    fun givenFragmentToActivityOverride_whenFragmentIsNested_whenActivityIsLaunchedFrom_thenOverrideIsCalled() {
        var launchOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, GenericActivity>(
                open = {
                    launchOverrideCalled = true
                    defaultOpen<GenericActivity>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity>()

        assertTrue(launchOverrideCalled)
    }

    @Test
    fun givenFragmentToActivityOverride_whenFragmentIsNested_whenActivityIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, GenericActivity>(
                open = {
                    closeOverrideCalled = true
                    defaultOpen<GenericActivity>().invoke(it)
                }
            )
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity>()
            .getNavigationHandle()
            .close()

        expectFragment<ActivityChildFragment>()

        assertTrue(closeOverrideCalled)
    }

}