package dev.enro.core

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import junit.framework.Assert.*
import nav.enro.*
import org.junit.Test

class UnboundActivitiesTest {

    @Test
    fun whenUnboundActivityIsOpened_thenNavigationKeyIsUnbound() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.startActivity(Intent(it, UnboundActivity::class.java))
        }
        val unboundActivity = expectActivity<UnboundActivity>()
        val unboundHandle = unboundActivity.getNavigationHandle()

        lateinit var caught: Throwable
        try {
            val key = unboundHandle.key
        }
        catch (t: Throwable) {
            caught = t
        }
        assertTrue(caught is IllegalStateException)
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
}