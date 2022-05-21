package dev.enro.core.overrides

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.core.*
import dev.enro.core.controller.navigationController
import dev.enro.core.legacy.*
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FragmentToFragmentOverrideTests() {

    lateinit var initialScenario: ActivityScenario<ActivityWithFragments>

    @Before
    fun before() {
        val intent = Intent(application, ActivityWithFragments::class.java)
            .addOpenInstruction(
                NavigationInstruction.Forward(
                    ActivityWithFragmentsKey(id = "initial activity")
                )
            )

        initialScenario = ActivityScenario.launch<ActivityWithFragments>(intent)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsStandalone_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<GenericFragment, ActivityChildFragment> {
                preOpened { preOpenCalled = true }
                postOpened { postOpenCalled = true }
                opened {
                    openCalled = true
                    defaultOpened(it)
                }
            }
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle()
            .forward(ActivityChildFragmentKey("override test 2"))

        expectFragment<ActivityChildFragment>()

        assertTrue(preOpenCalled)
        assertTrue(openCalled)
        assertTrue(postOpenCalled)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsStandalone_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        var preCloseCalled = false
        application.navigationController.addOverride(
            createOverride<GenericFragment, ActivityChildFragment> {
                preClosed { preCloseCalled = true }
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
            }
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
        assertTrue(preCloseCalled)
    }


    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsStandalone_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, GenericFragment> {
                preOpened { preOpenCalled = true }
                postOpened { postOpenCalled = true }
                opened {
                    openCalled = true
                    defaultOpened(it)
                }
            }
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .forward(GenericFragmentKey("override test 2"))

        expectFragment<GenericFragment>()

        waitFor { preOpenCalled }
        waitFor { openCalled }
        waitFor { postOpenCalled }
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsStandalone_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        var preCloseCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, GenericFragment> {
                preClosed { preCloseCalled = true }
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
            }
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

        waitFor { closeOverrideCalled }
        waitFor { preCloseCalled }
    }


    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsNested_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, ActivityChildFragmentTwo> {
                preOpened { preOpenCalled = true }
                postOpened { postOpenCalled = true }
                opened {
                    openCalled = true
                    defaultOpened(it)
                }
            }
        )

        initialScenario.getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test"))

        expectFragment<ActivityChildFragment>()
            .getNavigationHandle()
            .forward(ActivityChildFragmentTwoKey("override test 2"))

        expectFragment<ActivityChildFragmentTwo>()

        assertTrue(preOpenCalled)
        assertTrue(openCalled)
        assertTrue(postOpenCalled)
    }

    @Test
    fun givenFragmentToFragmentOverride_whenFragmentIsNested_andTargetIsNested_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        var preCloseCalled = false
        application.navigationController.addOverride(
            createOverride<ActivityChildFragment, ActivityChildFragmentTwo> {
                preClosed { preCloseCalled = true }
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
            }
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
        assertTrue(preCloseCalled)
    }

}