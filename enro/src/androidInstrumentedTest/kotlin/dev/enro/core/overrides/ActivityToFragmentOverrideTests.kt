@file:Suppress("DEPRECATION")
package dev.enro.core.overrides

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.core.*
import dev.enro.core.controller.navigationController
import dev.enro.core.legacy.ActivityChildFragment
import dev.enro.core.legacy.ActivityChildFragmentKey
import dev.enro.core.legacy.ActivityWithFragments
import dev.enro.core.legacy.ActivityWithFragmentsKey
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ActivityToFragmentOverrideTests() {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

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