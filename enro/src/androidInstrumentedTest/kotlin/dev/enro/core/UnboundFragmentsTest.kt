@file:Suppress("DEPRECATION")
package dev.enro.core

import androidx.fragment.app.commitNow
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class  UnboundFragmentsTest {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenUnboundFragmentIsOpened_thenNavigationKeyIsNoNavigationKey() {
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
        assertEquals("NoNavigationKey", unboundHandle.key::class.java.simpleName)
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
        unboundFragment.getNavigationHandle().present(GenericActivityKey("opened-from-unbound"))

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
        unboundFragment.getNavigationHandle().push(GenericFragmentKey("opened-from-unbound"))

        val genericActivity = expectFragment<GenericFragment>()
        assertEquals("opened-from-unbound", genericActivity.getNavigationHandle().asTyped<GenericFragmentKey>().key.id)
    }
}