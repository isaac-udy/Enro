@file:Suppress("DEPRECATION")
package dev.enro.core.legacy

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.core.asTyped
import dev.enro.core.close
import dev.enro.core.forward
import dev.enro.core.getNavigationHandle
import junit.framework.TestCase
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.*

class FragmentToFragmentTests {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenFragmentOpensFragment_andFragmentIsInAHost_thenFragmentIsLaunchedIntoHost() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(ActivityChildFragmentKey(id))

        val parentFragment = expectFragment<ActivityChildFragment>()
        val id2 = UUID.randomUUID().toString()
        parentFragment.getNavigationHandle().forward(ActivityChildFragmentTwoKey(id2))

        val childFragment = expectFragment<ActivityChildFragmentTwo>()
        val fragmentHandle = childFragment.getNavigationHandle().asTyped<ActivityChildFragmentTwoKey>()
        TestCase.assertEquals(id2, fragmentHandle.key.id)
    }

    @Test
    fun whenFragmentOpensFragment_andFragmentIsNotInAHost_thenFragmentIsLaunchedAsFullscreenDialogFragment() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(ActivityChildFragmentKey(id))

        val dialogFragment = expectFragmentHostForPresentableFragment()
        val parentFragment = dialogFragment.childFragmentManager.primaryNavigationFragment!!
        val id2 = UUID.randomUUID().toString()
        parentFragment.getNavigationHandle().forward(ActivityChildFragmentTwoKey(id2))

        val childFragment = expectContext<Fragment, ActivityChildFragmentTwoKey>().context
        val fragmentHandle = childFragment.getNavigationHandle().asTyped<ActivityChildFragmentTwoKey>()
        assertEquals(id2, fragmentHandle.key.id)
        assertTrue(childFragment.parentFragmentManager.primaryNavigationFragment == childFragment)
        assertTrue(childFragment.parentFragment?.parentFragmentManager?.primaryNavigationFragment == childFragment.parentFragment)
    }

    @Test
    fun whenFragmentOpensFragment_andFragmentIsInAHost_andIsDestroyed_thenClosingChildFragmentCreatesNewParentFragment() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = "UUID.randomUUID().toString()"
        handle.forward(ActivityChildFragmentKey(id))

        val parentFragment = expectFragment<ActivityChildFragment>()
        val id2 = UUID.randomUUID().toString()
        parentFragment.getNavigationHandle().forward(ActivityChildFragmentTwoKey(id2))

        val parentFragmentManager = parentFragment.parentFragmentManager

        val childFragment = expectFragment<ActivityChildFragmentTwo>()
        val fragmentHandle = childFragment.getNavigationHandle().asTyped<ActivityChildFragmentTwoKey>()
        TestCase.assertEquals(id2, fragmentHandle.key.id)

        // This will destroy the parent fragment, making it unavailable to re-use on close
        parentFragmentManager.commit { remove(parentFragment) }

        childFragment.getNavigationHandle().close()
        val newParentFragment = expectFragment<ActivityChildFragment>()
        TestCase.assertEquals(id, newParentFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>().key.id)
        TestCase.assertNotSame(parentFragment, newParentFragment)
    }
}