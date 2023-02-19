package dev.enro.core.legacy

import androidx.test.core.app.ActivityScenario
import dev.enro.GenericComposableKey
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.forward
import dev.enro.core.getNavigationHandle
import dev.enro.expectContext
import dev.enro.expectFragment
import dev.enro.getNavigationHandle
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test
import java.util.*

class FragmentToComposableTests {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

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