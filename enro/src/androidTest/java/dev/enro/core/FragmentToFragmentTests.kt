package dev.enro.core

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.expectFragment
import junit.framework.TestCase
import org.junit.Test
import java.util.*

private fun expectSingleFragmentActivity(): FragmentActivity {
    return expectActivity { it::class.java.simpleName == "SingleFragmentActivity"}
}

class FragmentToFragmentTests {

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
    fun whenFragmentOpensFragment_andFragmentIsNotInAHost_thenFragmentIsLaunchedAsSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(ActivityChildFragmentKey(id))

        val activity = expectSingleFragmentActivity()
        val parentFragment = activity.supportFragmentManager.primaryNavigationFragment!!
        val id2 = UUID.randomUUID().toString()
        parentFragment.getNavigationHandle().forward(ActivityChildFragmentTwoKey(id2))

        val activity2 = expectSingleFragmentActivity()
        val childFragment = activity2.supportFragmentManager.primaryNavigationFragment!!
        val fragmentHandle = childFragment.getNavigationHandle().asTyped<ActivityChildFragmentTwoKey>()
        TestCase.assertEquals(id2, fragmentHandle.key.id)
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