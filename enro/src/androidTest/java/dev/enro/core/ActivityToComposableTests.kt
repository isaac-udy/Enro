package dev.enro.core

import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.core.compose.ComposableDestination
import junit.framework.TestCase
import org.junit.Test
import java.util.*

private fun expectSingleFragmentActivity(): FragmentActivity {
    return expectActivity { it::class.java.simpleName == "SingleFragmentActivity" }
}

class ActivityToComposableTests {

    @Test
    fun whenActivityOpensComposable_andActivityDoesNotHaveComposeContainer_thenComposableIsLaunchedAsComposableFragmentHost() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(GenericComposableKey(id))

        expectSingleFragmentActivity()
        expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == id
        }
    }
}