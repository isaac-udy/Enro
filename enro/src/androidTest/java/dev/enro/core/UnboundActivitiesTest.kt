@file:Suppress("DEPRECATION")
package dev.enro.core

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.*
import dev.enro.core.controller.navigationController
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.TestCase
import org.junit.Test

class UnboundActivitiesTest {

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
        unboundActivity.getNavigationHandle().forward(GenericActivityKey("opened-from-unbound"))

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
        unboundActivity.getNavigationHandle().forward(GenericFragmentKey("opened-from-unbound"))

        val genericActivity = expectFragment<GenericFragment>()
        assertEquals("opened-from-unbound", genericActivity.getNavigationHandle().asTyped<GenericFragmentKey>().key.id)
    }


    @Test
    fun givenUnboundActivity_andActivityExecutorOverrideForUnboundActivity_whenBackButtonIsPressed_thenActivityIsClosed() {
        var overrideWasCalled = false
        val override = createOverride<Any, UnboundActivity> {
            closed {
                overrideWasCalled = true
                defaultClosed(it)
            }
        }
        val navigationController = (InstrumentationRegistry.getInstrumentation().context.applicationContext as Application)
            .navigationController

        navigationController.addOverride(override)
        try {
            val scenario = ActivityScenario.launch(UnboundActivity::class.java)
            scenario.onActivity { it.onBackPressed() }
            expectNoActivity()
        } finally {
            navigationController.removeOverride(override)
        }
        TestCase.assertTrue(overrideWasCalled)
    }
}