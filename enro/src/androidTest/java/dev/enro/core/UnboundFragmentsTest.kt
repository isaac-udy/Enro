package dev.enro.core

import androidx.fragment.app.commitNow
import androidx.test.core.app.ActivityScenario
import junit.framework.Assert.*
import dev.enro.*
import org.junit.Ignore
import org.junit.Test

class  UnboundFragmentsTest {

    @Test
    fun whenUnboundFragmentIsOpened_thenNavigationKeyIsUnbound() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.supportFragmentManager.commitNow {
                val fragment = UnboundFragment()
                replace(android.R.id.content, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
        val unboundFragment = expectFragment<UnboundFragment>()
        val unboundHandle = unboundFragment.getNavigationHandle()

        lateinit var caught: Throwable
        try {
            val key = unboundHandle.key
        }
        catch (t: Throwable) {
            caught = t
        }
        assertTrue(caught is IllegalStateException)
        assertNotNull(caught.message)
        assertTrue(caught.message!!.matches(Regex("The navigation handle for the context UnboundFragment.*has no NavigationKey")))
    }

    @Test
    fun whenUnboundFragmentIsOpened_thenUnboundActivityHasAnId() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.supportFragmentManager.commitNow {
                val fragment = UnboundFragment()
                replace(android.R.id.content, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
        val unboundFragment = expectFragment<UnboundFragment>()
        val unboundHandle = unboundFragment.getNavigationHandle()

        assertNotNull(unboundHandle.id)
    }

    @Test
    fun givenUnboundFragment_whenNavigationHandleIsUsedToClose_thenFragmentClosesCorrectly() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.supportFragmentManager.commitNow {
                val fragment = UnboundFragment()
                replace(android.R.id.content, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
        val unboundFragment = expectFragment<UnboundFragment>()
        unboundFragment.getNavigationHandle().close()

        val defaultActivity = expectActivity<DefaultActivity>()
        val fragmentWasRemoved = expectNoFragment<UnboundFragment>()
        assertNotNull(defaultActivity)
        assertTrue(fragmentWasRemoved)
    }

    @Test
    fun givenUnboundFragment_whenNavigationHandleIsUsedToOpenActivityKey_thenActivityIsOpenedCorrectly() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.supportFragmentManager.commitNow {
                val fragment = UnboundFragment()
                replace(android.R.id.content, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
        val unboundFragment = expectFragment<UnboundFragment>()
        unboundFragment.getNavigationHandle().forward(GenericActivityKey("opened-from-unbound"))

        val genericActivity = expectActivity<GenericActivity>()
        assertEquals("opened-from-unbound", genericActivity.getNavigationHandle().asTyped<GenericActivityKey>().key.id)
    }

    @Test
    fun givenUnboundFragment_whenNavigationHandleIsUsedToOpenFragmentKey_thenFragmentIsOpenedCorrectly() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        scenario.onActivity {
            it.supportFragmentManager.commitNow {
                val fragment = UnboundFragment()
                replace(android.R.id.content, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
        val unboundFragment = expectFragment<UnboundFragment>()
        unboundFragment.getNavigationHandle().forward(GenericFragmentKey("opened-from-unbound"))

        val genericActivity = expectFragment<GenericFragment>()
        assertEquals("opened-from-unbound", genericActivity.getNavigationHandle().asTyped<GenericFragmentKey>().key.id)
    }
}