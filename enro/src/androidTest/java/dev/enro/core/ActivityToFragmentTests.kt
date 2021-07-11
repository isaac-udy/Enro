package dev.enro.core

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import junit.framework.TestCase.assertEquals
import kotlinx.parcelize.Parcelize
import org.junit.Test
import java.util.*

private fun expectSingleFragmentActivity(): FragmentActivity {
    return expectActivity { it::class.java.simpleName == "SingleFragmentActivity"}
}

class ActivityToFragmentTests {

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
        val fragmentHandle = activeFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>()
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
        val fragmentHandle = activeFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>()
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
        val fragmentHandle = activeFragment.getNavigationHandle().asTyped<ActivityChildFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityOpensTwoFragmentsImmediatelyIntoDifferentContainers_thenBothFragmentsAreCorrectlyAddedToContainers() {
        val scenario = ActivityScenario.launch(ImmediateOpenChildActivity::class.java)

        var primary: String? = null
        var secondary: String? = null
        scenario.onActivity {
            primary = it.supportFragmentManager.findFragmentById(TestActivity.primaryFragmentContainer)!!
                .getNavigationHandle()
                .asTyped<GenericFragmentKey>()
                .key.id

            secondary = it.supportFragmentManager.findFragmentById(TestActivity.secondaryFragmentContainer)!!
                .getNavigationHandle()
                .asTyped<GenericFragmentKey>()
                .key.id
        }

        assertEquals("one", primary)
        assertEquals("two", secondary)
    }
}

@Parcelize
class ImmediateOpenChildActivityKey : NavigationKey
@NavigationDestination(ImmediateOpenChildActivityKey::class)
class ImmediateOpenChildActivity : TestActivity() {
    private val navigation by navigationHandle<ImmediateOpenChildActivityKey> {
        defaultKey(ImmediateOpenChildActivityKey())
        container(primaryFragmentContainer) {
            it is GenericFragmentKey && it.id == "one"
        }
        container(secondaryFragmentContainer) {
            it is GenericFragmentKey && it.id == "two"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation.forward(GenericFragmentKey("one"))
        navigation.forward(GenericFragmentKey("two"))
    }
}