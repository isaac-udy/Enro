@file:Suppress("DEPRECATION")
package dev.enro.core.overrides

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.core.*
import dev.enro.core.controller.navigationController
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class ActivityToActivityOverrideTests() {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenActivityToActivityOverride_whenInitialActivityOpenedWithDefaultKey_whenActivityIsLaunched_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<DefaultActivity, GenericActivity> {
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
            .forward(GenericActivityKey("override test"))

        expectActivity<GenericActivity>()

        waitFor { preOpenCalled }
        waitFor { openCalled }
        waitFor { postOpenCalled }
    }

    @Test
    fun givenActivityToActivityOverride_whenInitialActivityOpenedWithDefaultKey_whenActivityIsClosed_thenOverrideIsCalled() {
        var preCloseCalled = false
        var closeOverrideCalled = false
        application.navigationController.addOverride (
            createOverride<DefaultActivity, GenericActivity> {
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
                preClosed {
                    preCloseCalled = true
                }
            }
        )

        ActivityScenario.launch(DefaultActivity::class.java)
            .getNavigationHandle<DefaultActivityKey>()
            .forward(GenericActivityKey("override test"))

        expectActivity<GenericActivity>()
            .getNavigationHandle()
            .close()

        expectActivity<DefaultActivity>()

        waitFor { closeOverrideCalled }
        waitFor { preCloseCalled }
    }

    @Test
    fun givenActivityToActivityOverride_whenActivityIsLaunched_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<GenericActivity, GenericActivity>{
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
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity> { it.getNavigationHandle().asTyped<GenericActivityKey>().key.id == "override test 2" }

        waitFor { preOpenCalled }
        waitFor { openCalled }
        waitFor { postOpenCalled }
    }

    @Test
    fun givenActivityToActivityOverride_whenActivityIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        var preCloseCalled = false

        application.navigationController.addOverride(
            createOverride<GenericActivity, GenericActivity> {
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
                preClosed { preCloseCalled = true }
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
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity> { it.getNavigationHandle().asTyped<GenericActivityKey>().key.id == "override test 2" }
            .getNavigationHandle()
            .close()

        expectActivity<GenericActivity>()

        waitFor { closeOverrideCalled }
        waitFor { preCloseCalled }
    }


    @Test
    fun givenUnboundActivityToActivityOverride_whenActivityIsLaunched_thenOverrideIsCalled() {
        var preOpenCalled = false
        var openCalled = false
        var postOpenCalled = false

        application.navigationController.addOverride(
            createOverride<UnboundActivity, GenericActivity>{
                preOpened { preOpenCalled = true }
                postOpened { postOpenCalled = true }
                opened {
                    openCalled = true
                    defaultOpened(it)
                }
            }
        )

        ActivityScenario.launch(UnboundActivity::class.java)
        expectActivity<UnboundActivity>().getNavigationHandle()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity>()

        waitFor { preOpenCalled }
        waitFor { openCalled }
        waitFor { postOpenCalled }
    }

    @Test
    fun givenUnboundActivityToActivityOverride_whenActivityIsClosed_thenOverrideIsCalled() {
        var closeOverrideCalled = false
        var preCloseCalled = false

        application.navigationController.addOverride(
            createOverride<UnboundActivity, GenericActivity> {
                closed {
                    closeOverrideCalled = true
                    defaultClosed(it)
                }
                preClosed { preCloseCalled = true }
            }
        )

        ActivityScenario.launch(UnboundActivity::class.java)
        expectActivity<UnboundActivity>().getNavigationHandle()
            .forward(GenericActivityKey("override test 2"))

        expectActivity<GenericActivity> { it.getNavigationHandle().asTyped<GenericActivityKey>().key.id == "override test 2" }
            .getNavigationHandle()
            .close()

        expectActivity<UnboundActivity>()

        waitFor { closeOverrideCalled }
        waitFor { preCloseCalled }
    }
}