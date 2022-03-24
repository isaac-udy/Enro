package dev.enro.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.parcelize.Parcelize
import org.junit.Test
import java.util.*

private fun expectSingleFragmentActivity(): FragmentActivity {
    return expectActivity { it::class.java.simpleName == "SingleFragmentActivity" }
}

class ActivityToFragmentTests {

    @Test
    fun whenActivityIsNotAFragmentActivity_thenFragmentNavigationOpensSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity {
            it.getNavigationHandle().forward(GenericFragmentKey("fragment from component activity"))
        }
        expectSingleFragmentActivity()
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
    fun whenActivityOpensFragment_andActivityDoesNotHaveFragmentHost_thenFragmentIsLaunchedAsSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(GenericFragmentKey(id))

        val activity = expectSingleFragmentActivity()
        val activeFragment = activity.supportFragmentManager.primaryNavigationFragment!!
        val fragmentHandle = activeFragment.getNavigationHandle().asTyped<GenericFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityOpensFragmentWithChildrenStack_andActivityDoesNotHaveFragmentHost_thenFragmentAndChildrenAreLaunchedAsSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(
            GenericFragmentKey("1"),
            GenericFragmentKey("2"),
            GenericFragmentKey(id)
        )

        val activity = expectSingleFragmentActivity()
        val fragment = expectFragment<GenericFragment>()

        val fragmentHandle = fragment.getNavigationHandle().asTyped<GenericFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
        assertEquals(fragment, activity.supportFragmentManager.primaryNavigationFragment!!)
    }


    @Test
    fun whenActivityOpensFragment_andActivityHasFragmentHostForFragment_thenFragmentIsLaunchedIntoHost() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(ActivityChildFragmentKey(id))

        expectActivity<ActivityWithFragments>()
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

        val activity = expectSingleFragmentActivity()
        val activeFragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle =
            activeFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)

        fragmentHandle.close()
        expectNoActivity()
    }


    @Test
    fun whenActivityOpensFragment_andActivityHasFragmentHostThatDoesNotAcceptFragment_thenFragmentIsLaunchedAsSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(GenericFragmentKey(id))

        val activity = expectSingleFragmentActivity()
        val activeFragment = activity.supportFragmentManager.primaryNavigationFragment!!
        val fragmentHandle = activeFragment.getNavigationHandle().asTyped<GenericFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityOpensFragmentAsReplacement_andActivityHasFragmentHostForFragment_thenFragmentIsLaunchedAsSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.replace(ActivityChildFragmentKey(id))

        val activity = expectSingleFragmentActivity()
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
            .forward(ActivityChildFragmentKey(UUID.randomUUID().toString()))

        val activityHandle = expectActivity<ActivityWithFragments>().getNavigationHandle()
        val fragmentHandle = expectFragment<ActivityChildFragment>().getNavigationHandle()

        scenario.onActivity {
            activityHandle
                .forward(ActivityChildFragmentKey("one"))

            activityHandle
                .forward(ActivityChildFragmentKey("two"))

            fragmentHandle
                .forward(ActivityChildFragmentKey("three"))

            activityHandle
                .forward(ActivityChildFragmentKey("four"))
        }

        val id = expectFragment<ActivityChildFragment>().getNavigationHandle().asTyped<ActivityChildFragmentKey>().key.id
        assertEquals("four", id)
    }

    @Test
    fun givenActivityWithChildFragment_whenFragmentIsDetached_andStaleFragmentNavigationHandleIsUsedForNavigation_thenNothingHappens() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        expectActivity<ActivityWithFragments>()
            .getNavigationHandle()
            .forward(ActivityChildFragmentKey(UUID.randomUUID().toString()))

        val fragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle = fragment.getNavigationHandle()

        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .detach(fragment)
                .commitNow()

            fragmentHandle.forward(ActivityChildFragmentKey("should not appear"))
        }

        assertNull(expectActivity<ActivityWithFragments>().supportFragmentManager.primaryNavigationFragment)
    }
}

@Parcelize
class ImmediateOpenChildActivityKey : NavigationKey

@NavigationDestination(ImmediateOpenChildActivityKey::class)
class ImmediateOpenChildActivity : TestActivity() {
    private val navigation by navigationHandle<ImmediateOpenChildActivityKey> {
        defaultKey(ImmediateOpenChildActivityKey())
    }

    val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is GenericFragmentKey && it.id == "one"
    }

    val secondaryContainer by navigationContainer(secondaryFragmentContainer) {
        it is GenericFragmentKey && it.id == "two"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation.forward(GenericFragmentKey("one"))
        navigation.forward(GenericFragmentKey("two"))
    }
}

@Parcelize
class ImmediateOpenFragmentChildActivityKey : NavigationKey

@NavigationDestination(ImmediateOpenFragmentChildActivityKey::class)
class ImmediateOpenFragmentChildActivity : TestActivity() {
    private val navigation by navigationHandle<ImmediateOpenFragmentChildActivityKey> {
        defaultKey(ImmediateOpenFragmentChildActivityKey())
    }

    val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is ImmediateOpenChildFragmentKey && it.name == "one"
    }

    val secondaryContainer by navigationContainer(secondaryFragmentContainer) {
        it is ImmediateOpenChildFragmentKey && it.name == "two"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation.forward(ImmediateOpenChildFragmentKey("one"))
        navigation.forward(ImmediateOpenChildFragmentKey("two"))
    }
}


@Parcelize
data class ImmediateOpenChildFragmentKey(val name: String) : NavigationKey

@NavigationDestination(ImmediateOpenChildFragmentKey::class)
class ImmediateOpenChildFragment : TestFragment() {
    private val navigation by navigationHandle<ImmediateOpenChildFragmentKey>()

    val primaryContainer by navigationContainer(primaryFragmentContainer) {
        it is GenericFragmentKey && it.id == "one"
    }

    val secondaryContainer by navigationContainer(secondaryFragmentContainer) {
        it is GenericFragmentKey && it.id == "two"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation.forward(GenericFragmentKey("one"))
        navigation.forward(GenericFragmentKey("two"))
    }
}