package dev.enro.core.overrides

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import junit.framework.Assert.assertTrue
import nav.enro.*
import nav.enro.core.*
import nav.enro.core.controller.navigationController
import org.junit.Test

class ActivityToFragmentOverrideTests() {

    @Test
    fun givenActivityToFragmentOverride_andActivityDoesNotSupportFragment_whenInitialActivityOpenedWithDefaultKey_whenFragmentIsLaunched_whenActivityDoes_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<DefaultActivity, GenericFragment> {
                preOpened { preOpenCalled = true }
                postOpened { postOpenCalled = true }
                opened {
                    openCalled = true
                    defaultOpened(it)
                }
            }
        )
        ActivityScenario.launch(DefaultActivity::class.java)
            .getNavigationHandle<DefaultActivityKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()

        assertTrue(preOpenCalled)
        assertTrue(openCalled)
        assertTrue(postOpenCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_andActivityDoesNotSupportFragment_whenInitialActivityOpenedWithDefaultKey_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        var preCloseCalled = false

        application.navigationController.addOverride(
            createOverride<DefaultActivity, GenericFragment> {
                preClosed { preCloseCalled = true }
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
            }
        )

        ActivityScenario.launch(DefaultActivity::class.java)
            .getNavigationHandle<DefaultActivityKey>()
            .forward(GenericFragmentKey("override test"))

        expectFragment<GenericFragment>()
            .getNavigationHandle().close()

        expectActivity<DefaultActivity>()

        assertTrue(closeOverrideCalled)
        assertTrue(preCloseCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_andActivityDoesNotSupportFragment_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<GenericActivity, GenericFragment> {
                preOpened { preOpenCalled = true }
                postOpened { postOpenCalled = true }
                opened {
                    openCalled = true
                    defaultOpened(it)
                }
            }
        )
        val intent = Intent(application, GenericActivity::class.java)
            .addOpenInstruction(
                NavigationInstruction.Forward(
                    GenericActivityKey(id = "override test")
                )
            )

        ActivityScenario.launch<GenericActivity>(intent)
            .getNavigationHandle<GenericActivityKey>()
            .forward(GenericFragmentKey("override test 2"))

        expectFragment<GenericFragment>()

        assertTrue(preOpenCalled)
        assertTrue(openCalled)
        assertTrue(postOpenCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_andActivityDoesNotSupportFragment_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        var preCloseCalled = false

        application.navigationController.addOverride(
            createOverride<GenericActivity, GenericFragment> {
                preClosed { preCloseCalled = true }
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
            }
        )

        val intent = Intent(application, GenericActivity::class.java)
            .addOpenInstruction(
                NavigationInstruction.Forward(
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
        assertTrue(preCloseCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_whenFragmentIsLaunched_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<ActivityWithFragments, ActivityChildFragment> {
                preOpened { preOpenCalled = true }
                postOpened { postOpenCalled = true }
                opened {
                    openCalled = true
                    defaultOpened(it)
                }
            }
        )
        val intent = Intent(application, ActivityWithFragments::class.java)
            .addOpenInstruction(
                NavigationInstruction.Forward(
                    ActivityWithFragmentsKey(id = "override test")
                )
            )

        ActivityScenario.launch<ActivityWithFragments>(intent)
            .getNavigationHandle<ActivityWithFragmentsKey>()
            .forward(ActivityChildFragmentKey("override test 2"))

        expectFragment<ActivityChildFragment>()

        assertTrue(preOpenCalled)
        assertTrue(openCalled)
        assertTrue(postOpenCalled)
    }

    @Test
    fun givenActivityToFragmentOverride_whenFragmentIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        var preCloseCalled = false

        application.navigationController.addOverride(
            createOverride<ActivityWithFragments, ActivityChildFragment> {
                preClosed { preCloseCalled = true }
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
            }
        )
        val intent = Intent(application, ActivityWithFragments::class.java)
            .addOpenInstruction(
                NavigationInstruction.Forward(
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
        assertTrue(preCloseCalled)
    }
}