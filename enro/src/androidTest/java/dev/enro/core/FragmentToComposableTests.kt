package dev.enro.core

import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.core.compose.ComposableDestination
import org.junit.Test
import java.util.*

class FragmentToComposableTests {

    @Test
    fun whenFragmentOpensComposable_andFragmentDoesNotHaveComposeContainer_thenComposableIsLaunchedAsComposableFragmentHost() {
        val scenario = ActivityScenario.launch(ActivityWithFragments::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithFragmentsKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(ActivityChildFragmentKey(id))

        val parentFragment = expectFragment<ActivityChildFragment>()

        parentFragment.getNavigationHandle().forward(GenericComposableKey(id))

        expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == id
        }
    }
}