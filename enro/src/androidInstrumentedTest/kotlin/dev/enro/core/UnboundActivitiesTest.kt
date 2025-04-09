@file:Suppress("DEPRECATION")
package dev.enro.core

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.DefaultActivity
import dev.enro.GenericActivity
import dev.enro.GenericActivityKey
import dev.enro.GenericFragment
import dev.enro.GenericFragmentKey
import dev.enro.UnboundActivity
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.createNavigationModule
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.navigationController
import dev.enro.expectActivity
import dev.enro.expectFragment
import dev.enro.expectNoActivity
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.TestCase
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class UnboundActivitiesTest {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenUnboundActivityIsOpened_thenNavigationKeyIsNoNavigationKey() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.startActivity(Intent(it, UnboundActivity::class.java))
        }
        val unboundActivity = expectActivity<UnboundActivity>()
        val unboundHandle = unboundActivity.getNavigationHandle()

        assertEquals("NoNavigationKey", unboundHandle.key::class.java.simpleName)
    }

    @Test
    fun whenUnboundActivityIsOpened_thenUnboundActivityHasAnId() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.startActivity(Intent(it, UnboundActivity::class.java))
        }
        val unboundActivity = expectActivity<UnboundActivity>()
        val unboundHandle = unboundActivity.getNavigationHandle()

        assertNotNull(unboundHandle.id)
    }

    @Test
    fun whenUnboundActivityIsRecreated_thenUnboundActivityIdIsStable() {
        val scenario = ActivityScenario.launch(UnboundActivity::class.java)
        val id = expectActivity<UnboundActivity>().getNavigationHandle().id
        scenario.recreate()

        val recreatedId = expectActivity<UnboundActivity>().getNavigationHandle().id

        assertEquals(id, recreatedId)
    }

    @Test
    fun givenUnboundActivity_whenNavigationHandleIsUsedToClose_thenActivityClosesCorrectly() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.startActivity(Intent(it, UnboundActivity::class.java))
        }
        val unboundActivity = expectActivity<UnboundActivity>()
        unboundActivity.getNavigationHandle().close()

        val defaultActivity = expectActivity<DefaultActivity>()
        assertNotNull(defaultActivity)
    }

    @Test
    fun givenUnboundActivity_whenNavigationHandleIsUsedToOpenActivityKey_thenActivityIsOpenedCorrectly() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.startActivity(Intent(it, UnboundActivity::class.java))
        }
        val unboundActivity = expectActivity<UnboundActivity>()
        unboundActivity.getNavigationHandle().present(GenericActivityKey("opened-from-unbound"))

        val genericActivity = expectActivity<GenericActivity>()
        assertEquals("opened-from-unbound", genericActivity.getNavigationHandle().asTyped<GenericActivityKey>().key.id)
    }

    @Test
    fun givenUnboundActivity_whenNavigationHandleIsUsedToOpenFragmentKey_thenFragmentIsOpenedCorrectly() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.startActivity(Intent(it, UnboundActivity::class.java))
        }
        val unboundActivity = expectActivity<UnboundActivity>()
        unboundActivity.getNavigationHandle().push(GenericFragmentKey("opened-from-unbound"))

        val genericActivity = expectFragment<GenericFragment>()
        assertEquals("opened-from-unbound", genericActivity.getNavigationHandle().asTyped<GenericFragmentKey>().key.id)
    }


    @Test
    fun givenUnboundActivity_andInterceptorForUnboundActivity_whenBackButtonIsPressed_thenActivityIsClosed() {
        var interceptorWasCalled = false
        val interceptorModule = createNavigationModule {
            interceptor(object : NavigationInstructionInterceptor {
                override fun intercept(
                    instruction: NavigationInstruction.Close,
                    context: NavigationContext<*>
                ): NavigationInstruction {
                    if (context.contextReference !is UnboundActivity) return instruction
                    interceptorWasCalled = true
                    return instruction
                }
            })
        }
        val navigationController = (InstrumentationRegistry.getInstrumentation().context.applicationContext as Application)
            .navigationController
            .apply {
                // This test specifically requires EnroBackConfiguration.Default
                @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
                setConfig (
                    config.copy(backConfiguration = EnroBackConfiguration.Default)
                )
            }

        navigationController.addModule(interceptorModule)
        ActivityScenario.launch(UnboundActivity::class.java)
        Espresso.pressBackUnconditionally()
        expectNoActivity()
        TestCase.assertTrue(interceptorWasCalled)
    }
}