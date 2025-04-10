@file:Suppress("DEPRECATION")
package dev.enro.core.legacy

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import dev.enro.DefaultActivity
import dev.enro.DefaultActivityKey
import dev.enro.GenericFragment
import dev.enro.GenericFragmentKey
import dev.enro.TestActivity
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.asTyped
import dev.enro.core.close
import dev.enro.core.container.accept
import dev.enro.core.directParentContainer
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationHandle
import dev.enro.core.parentContainer
import dev.enro.core.push
import dev.enro.core.replace
import dev.enro.core.toDisplayString
import dev.enro.expectActivity
import dev.enro.expectActivityHostForAnyInstruction
import dev.enro.expectContext
import dev.enro.expectFragment
import dev.enro.expectFragmentContext
import dev.enro.expectFragmentHostForPresentableFragment
import dev.enro.expectNoActivity
import dev.enro.expectNoFragment
import dev.enro.getNavigationHandle
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import leakcanary.SkipLeakDetection
import org.junit.Rule
import org.junit.Test
import java.util.*

class ActivityToFragmentTests {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenActivityIsNotAFragmentActivity_thenFragmentNavigationOpensSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity {
            it.getNavigationHandle().push(GenericFragmentKey("fragment from component activity"))
        }
        expectActivityHostForAnyInstruction()
        assertEquals(
            "fragment from component activity",
            expectFragment<GenericFragment>()
                .getNavigationHandle()
                .asTyped<GenericFragmentKey>()
                .key
                .id
        )
    }

    @Test
    fun whenActivityOpensFragment_andActivityDoesNotHaveFragmentHost_thenFragmentIsLaunchedAsFullscreenDialogFragment() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        val id = UUID.randomUUID().toString()
        handle.push(GenericFragmentKey(id))

        val activity = expectFragmentHostForPresentableFragment()
        val activeFragment = activity.childFragmentManager.primaryNavigationFragment!!
        val fragmentHandle = activeFragment.getNavigationHandle().asTyped<GenericFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityOpensFragment_andActivityHasFragmentHostForFragment_thenFragmentIsLaunchedIntoHost() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.push(ActivityChildFragmentKey(id))

        expectActivity<ActivityWithFragments>()
        val activeFragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle =
            activeFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }


    @Test
    fun whenActivityOpensFragment_andActivityHasFragmentHostForFragment_andFragmentContainerIsNotVisible_thenFragmentIsLaunchedIntoFullscreenDialogFragment() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()
        scenario.onActivity {
            it.findViewById<View>(TestActivity.primaryFragmentContainer).isVisible = false
            it.findViewById<View>(TestActivity.secondaryFragmentContainer).isVisible = false
        }

        val id = UUID.randomUUID().toString()
        handle.push(ActivityChildFragmentKey(id))

        expectFragmentHostForPresentableFragment()
        val activeFragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle =
            activeFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityReplacedByFragment_andActivityHasFragmentHostForFragment_thenFragmentIsLaunchedAsSingleActivity_andCloseLeavesNoActivityActive() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.replace(ActivityChildFragmentKey(id))

        expectActivityHostForAnyInstruction()
        val activeFragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle =
            activeFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)

        fragmentHandle.close()
        expectNoActivity()
    }


    @Test
    fun whenActivityOpensFragment_andActivityHasFragmentHostThatDoesNotAcceptFragment_thenFragmentIsLaunchedAsFullscreenDialogFragment() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.push(GenericFragmentKey(id))
        val context = expectFragmentContext<GenericFragmentKey>()
        Log.e("EnroTests", "Context: ${context.navigationContext.toDisplayString()}, ${context.navigationContext.directParentContainer()?.context?.toDisplayString()}, ${context.navigationContext.parentContainer()?.context?.parentContainer()?.context?.toDisplayString()}")
        val activity = expectFragmentHostForPresentableFragment()
        val activeFragment = activity.childFragmentManager.primaryNavigationFragment!!
        val fragmentHandle = activeFragment.getNavigationHandle().asTyped<GenericFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityOpensFragmentAsReplacement_andActivityHasFragmentHostForFragment_thenFragmentIsLaunchedAsFullscreenDialogFragment() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.replace(ActivityChildFragmentKey(id))

        val activity = expectActivityHostForAnyInstruction()
        val activeFragment = activity.supportFragmentManager.primaryNavigationFragment!!
        val fragmentHandle =
            activeFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityOpensTwoFragmentsImmediatelyIntoDifferentContainers_thenBothFragmentsAreCorrectlyAddedToContainers() {
        val scenario = ActivityScenario.launch(ImmediateOpenChildActivity::class.java)

        scenario.onActivity {
            assertEquals(
                "one",
                it.supportFragmentManager.findFragmentById(TestActivity.primaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )

            assertEquals(
                "two",
                it.supportFragmentManager.findFragmentById(TestActivity.secondaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )
        }
    }

    @Test
    fun whenActivityOpensTwoFragmentsImmediatelyIntoDifferentContainers_andThoseFragmentsOpenTwoChildrenImmediately_thenAllFragmentsAreOpenedCorrectly() {
        val scenario = ActivityScenario.launch(ImmediateOpenFragmentChildActivity::class.java)

        scenario.onActivity {
            val primary =
                it.supportFragmentManager.findFragmentById(TestActivity.primaryFragmentContainer)!!
            val secondary =
                it.supportFragmentManager.findFragmentById(TestActivity.secondaryFragmentContainer)!!

            assertEquals(
                "one", primary.childFragmentManager
                    .findFragmentById(TestFragment.primaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )

            assertEquals(
                "two", primary.childFragmentManager
                    .findFragmentById(TestFragment.secondaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )

            assertEquals(
                "one", secondary.childFragmentManager
                    .findFragmentById(TestFragment.primaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )

            assertEquals(
                "two", secondary.childFragmentManager
                    .findFragmentById(TestFragment.secondaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )
        }
    }

    /**
     * Executing navigation instructions as a response to fragment creation (i.e. in "onCreate") may cause issues
     * with attempting to access viewmodels from a detached fragment. This test should verify
     * that the behaviour of the test above will continue to work after activity re-creation
     */
    @Test
    fun whenActivityOpensTwoFragmentsImmediatelyIntoDifferentContainers_andThoseFragmentsOpenTwoChildrenImmediately_thenAllFragmentsAreOpenedCorrectly_recreated() {
        val scenario = ActivityScenario.launch(ImmediateOpenFragmentChildActivity::class.java)
        scenario.recreate()

        scenario.onActivity {
            val primary =
                it.supportFragmentManager.findFragmentById(TestActivity.primaryFragmentContainer)!!
            val secondary =
                it.supportFragmentManager.findFragmentById(TestActivity.secondaryFragmentContainer)!!

            assertEquals(
                "one", primary.childFragmentManager
                    .findFragmentById(TestFragment.primaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )

            assertEquals(
                "two", primary.childFragmentManager
                    .findFragmentById(TestFragment.secondaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )

            assertEquals(
                "one", secondary.childFragmentManager
                    .findFragmentById(TestFragment.primaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )

            assertEquals(
                "two", secondary.childFragmentManager
                    .findFragmentById(TestFragment.secondaryFragmentContainer)!!
                    .getNavigationHandle()
                    .asTyped<GenericFragmentKey>()
                    .key.id
            )
        }
    }

    @Test
    fun givenActivityWithChildFragment_whenMultipleChildrenAreOpenedOnActivity_andStaleChildFragmentHandleIsUsedToOpenAnotherChild_thenStaleNavigationActionIsIgnored_andOtherNavigationActionsSucceed() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        expectActivity<ActivityWithFragments>()
            .getNavigationHandle()
            .push(ActivityChildFragmentKey(UUID.randomUUID().toString()))

        val activityHandle = expectActivity<ActivityWithFragments>().getNavigationHandle()
        val fragmentHandle = expectFragment<ActivityChildFragment>().getNavigationHandle()

        scenario.onActivity {
            activityHandle
                .push(ActivityChildFragmentKey("one"))

            activityHandle
                .push(ActivityChildFragmentKey("two"))

            fragmentHandle
                .push(ActivityChildFragmentKey("three"))

            activityHandle
                .push(ActivityChildFragmentKey("four"))
        }

        val id = expectFragment<ActivityChildFragment>().getNavigationHandle().asTyped<ActivityChildFragmentKey>().key.id
        assertEquals("four", id)
    }

    @Test
    fun givenActivityWithChildFragment_whenFragmentIsDetached_andStaleFragmentNavigationHandleIsUsedForNavigation_thenNothingHappens() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        expectActivity<ActivityWithFragments>()
            .getNavigationHandle()
            .push(ActivityChildFragmentKey(UUID.randomUUID().toString()))

        val fragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle = fragment.getNavigationHandle()

        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .detach(fragment)
                .commitNow()

            fragmentHandle.push(ActivityChildFragmentKey("should not appear"))
        }

        assertNull(expectActivity<ActivityWithFragments>().supportFragmentManager.primaryNavigationFragment)
    }

    @Test
    fun givenFragmentOpenInActivity_whenFragmentIsClosedAfterInstanceStateIsSaved_thenNavigationIsNotClosed_untilActivityIsActiveAgain() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        expectActivity<ActivityWithFragments>()
            .getNavigationHandle()
            .push(ActivityChildFragmentKey(UUID.randomUUID().toString()))

        val fragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle = fragment.getNavigationHandle()

        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {
            assertTrue(it.supportFragmentManager.isStateSaved)
        }
        fragmentHandle.close()
        scenario.moveToState(Lifecycle.State.RESUMED)
        expectNoFragment<ActivityChildFragment>()
    }

    @SkipLeakDetection("""
        Moving the Activity into different states to check whether out-of-order navigation handle instructions
        occur correctly seems to give leak canary detection a bit of flakiness here; we end up detecting
        a leak from the Fragment's mContainer's View reference, which doesn't appear to happen in production.
    """)
    @Test
    fun givenFragmentOpenInActivity_whenFragmentIsClosedAfterInstanceStateIsSaved_thenNavigationIsNotClosed_untilActivityIsActiveAgain_recreation() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        expectActivity<ActivityWithFragments>()
            .getNavigationHandle()
            .push(ActivityChildFragmentKey(UUID.randomUUID().toString()))

        val fragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle = fragment.getNavigationHandle()

        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {
            assertTrue(it.supportFragmentManager.isStateSaved)
        }
        fragmentHandle.close()
        scenario.recreate()
        scenario.moveToState(Lifecycle.State.RESUMED)
        expectActivity<ActivityWithFragments>()
        expectNoFragment<ActivityChildFragment>()
    }

    @Test
    fun givenTwoFragmentsOpenInActivity_whenTopFragmentIsClosedAfterInstanceStateIsSaved_thenNavigationIsNotClosed_untilActivityIsActiveAgain_andCorrectFragmentIsActive() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val firstFragmentKey = ActivityChildFragmentKey(UUID.randomUUID().toString())
        val secondFragmentKey = ActivityChildFragmentKey(UUID.randomUUID().toString())

        expectActivity<ActivityWithFragments>()
            .getNavigationHandle()
            .push(firstFragmentKey)

        expectFragment<ActivityChildFragment> { it.getNavigationHandle().key == firstFragmentKey }
            .navigation
            .push(secondFragmentKey)

        val fragment = expectFragment<ActivityChildFragment> { it.getNavigationHandle().key == secondFragmentKey }
        val fragmentHandle = fragment.getNavigationHandle()

        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {
            assertTrue(it.supportFragmentManager.isStateSaved)
        }
        fragmentHandle.close()
        scenario.moveToState(Lifecycle.State.RESUMED)
        expectFragment<ActivityChildFragment> { it.getNavigationHandle().key == firstFragmentKey }
        expectNoFragment<ActivityChildFragment> { it.getNavigationHandle().key == secondFragmentKey }
    }

    @Test
    fun givenTwoFragmentsOpenInActivity_whenTopFragmentIsClosedAfterInstanceStateIsSaved_thenNavigationIsNotClosed_untilActivityIsActiveAgain_andCorrectFragmentIsActive_recreation() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val firstFragmentKey = ActivityChildFragmentKey(UUID.randomUUID().toString())
        val secondFragmentKey = ActivityChildFragmentKey(UUID.randomUUID().toString())

        expectActivity<ActivityWithFragments>()
            .getNavigationHandle()
            .push(firstFragmentKey)

        expectFragment<ActivityChildFragment> { it.getNavigationHandle().key == firstFragmentKey }
            .navigation
            .push(secondFragmentKey)

        val fragment = expectFragment<ActivityChildFragment> { it.getNavigationHandle().key == secondFragmentKey }
        val fragmentHandle = fragment.getNavigationHandle()

        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {
            assertTrue(it.supportFragmentManager.isStateSaved)
        }
        fragmentHandle.close()
        scenario.recreate()
        scenario.moveToState(Lifecycle.State.RESUMED)
        expectFragment<ActivityChildFragment> { it.getNavigationHandle().key == firstFragmentKey }
        expectNoFragment<ActivityChildFragment> { it.getNavigationHandle().key == secondFragmentKey }
    }

    // https://github.com/isaac-udy/Enro/issues/34
    /**
     * givenActivityOpensFragmentA
     * andFragmentAPerformsForwardNavigationToFragmentB
     * andFragmentBPerformsForwardNavigationToFragmentC
     *
     * whenActivityLaterPerformsForwardNavigationToFragmentD
     * andFragmentDIsClosed
     *
     * thenFragmentCIsActiveInContainer
     */
    @Test
    fun givenActivityOpensFragment_andFragmentOpensForward_thenActivityOpensAnotherFragment_thenContainerBackstackIsRetained() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val fragmentAKey = ActivityChildFragmentKey("Fragment A")
        val fragmentBKey = ActivityChildFragmentKey("Fragment B")
        val fragmentCKey = ActivityChildFragmentKey("Fragment C")
        val fragmentDKey = ActivityChildFragmentKey("Fragment D")

        val activity = expectActivity<ActivityWithFragments>()
        activity.getNavigationHandle()
            .push(fragmentAKey)

        expectContext<ActivityChildFragment, ActivityChildFragmentKey> { it.navigation.key == fragmentAKey }
            .navigation
            .push(fragmentBKey)

        expectContext<ActivityChildFragment, ActivityChildFragmentKey> { it.navigation.key == fragmentBKey }
            .navigation
            .push(fragmentCKey)

        expectContext<ActivityChildFragment, ActivityChildFragmentKey> { it.navigation.key == fragmentCKey }

        activity.getNavigationHandle()
            .push(fragmentDKey)

        expectContext<ActivityChildFragment, ActivityChildFragmentKey> { it.navigation.key == fragmentDKey }
            .navigation
            .close()

        expectContext<ActivityChildFragment, ActivityChildFragmentKey> { it.navigation.key == fragmentCKey }
    }
}

@Parcelize
class ImmediateOpenChildActivityKey : Parcelable, NavigationKey

@NavigationDestination(ImmediateOpenChildActivityKey::class)
class ImmediateOpenChildActivity : TestActivity() {
    private val primary by navigationContainer(
        containerId = primaryFragmentContainer,
        filter = accept {
            key<GenericFragmentKey> { it.id == "one" }
        }
    )
    private val secondary by navigationContainer(
        containerId = secondaryFragmentContainer,
        filter = accept {
            key<GenericFragmentKey> { it.id == "two" }
        }
    )
    private val navigation by navigationHandle<ImmediateOpenChildActivityKey> {
        defaultKey(ImmediateOpenChildActivityKey())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation.push(GenericFragmentKey("one"))
        navigation.push(GenericFragmentKey("two"))
    }
}

@Parcelize
class ImmediateOpenFragmentChildActivityKey : Parcelable, NavigationKey

@NavigationDestination(ImmediateOpenFragmentChildActivityKey::class)
class ImmediateOpenFragmentChildActivity : TestActivity() {
    private val primary by navigationContainer(
        containerId = primaryFragmentContainer,
        filter = accept {
            key<ImmediateOpenChildFragmentKey> { it.name == "one" }
        }
    )
    private val secondary by navigationContainer(
        containerId = secondaryFragmentContainer,
        filter = accept {
            key<ImmediateOpenChildFragmentKey> { it.name == "two" }
        }
    )

    private val navigation by navigationHandle<ImmediateOpenFragmentChildActivityKey> {
        defaultKey(ImmediateOpenFragmentChildActivityKey())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation.push(ImmediateOpenChildFragmentKey("one"))
        navigation.push(ImmediateOpenChildFragmentKey("two"))
    }
}


@Parcelize
data class ImmediateOpenChildFragmentKey(val name: String) : Parcelable, NavigationKey.SupportsPush

@NavigationDestination(ImmediateOpenChildFragmentKey::class)
class ImmediateOpenChildFragment : TestFragment() {
    private val primary by navigationContainer(
        containerId = primaryFragmentContainer,
        filter = accept {
            key<GenericFragmentKey> { it.id == "one" }
        }
    )
    private val secondary by navigationContainer(
        containerId = secondaryFragmentContainer,
        filter = accept {
            key<GenericFragmentKey> { it.id == "two" }
        }
    )
    private val navigation by navigationHandle<ImmediateOpenChildFragmentKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation.push(GenericFragmentKey("one"))
        navigation.push(GenericFragmentKey("two"))
    }
}