package nav.enro.core

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.*
import nav.enro.core.internal.SingleFragmentActivity
import nav.enro.core.internal.context.navigationContext
import org.junit.Test
import java.util.*

class ActivityToFragmentTests {

    @Test
    fun whenActivityOpensFragment_andActivityDoesNotHaveFragmentHost_thenFragmentIsLaunchedAsSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.navigationHandle<DefaultActivityKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(GenericFragmentKey(id))

        val activity = expectActivity<SingleFragmentActivity>()
        val host = activity.navigationContext.activeFragmentHost!!

        val activeFragment = host.fragmentManager.findFragmentById(host.containerView)!!
        val fragmentHandle by activeFragment.navigationHandle<GenericFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityOpensFragment_andActivityHasFragmentHostForFragment_thenFragmentIsLaunchedIntoHost() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.navigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(ActivityChildFragmentKey(id))

        expectActivity<ActivityWithFragments>()
        val activeFragment = expectFragment<ActivityChildFragment>()
        val fragmentHandle by activeFragment.navigationHandle<ActivityChildFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }

    @Test
    fun whenActivityOpensFragmentAsReplacement_andActivityHasFragmentHostForFragment_thenFragmentIsLaunchedAsSingleFragmentActivity() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.navigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.replace(ActivityChildFragmentKey(id))

        val activity = expectActivity<SingleFragmentActivity>()
        val host = activity.navigationContext.activeFragmentHost!!

        val activeFragment = host.fragmentManager.findFragmentById(host.containerView)!!
        val fragmentHandle by activeFragment.navigationHandle<ActivityChildFragmentKey>()
        assertEquals(id, fragmentHandle.key.id)
    }
}