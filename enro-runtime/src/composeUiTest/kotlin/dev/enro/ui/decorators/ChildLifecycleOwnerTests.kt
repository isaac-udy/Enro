package dev.enro.ui.decorators

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import kotlin.test.Test
import kotlin.test.assertEquals

// LifecycleRegistry enforces the main thread, which the Compose UI test's UI thread provides.
@OptIn(ExperimentalTestApi::class)
class ChildLifecycleOwnerTests {

    @Test
    fun `parent destroyed before a cap is applied moves the child to DESTROYED`() = runComposeUiTest {
        runOnUiThread {
            val child = ChildLifecycleOwner()

            child.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            child.handleLifecycleEvent(Lifecycle.Event.ON_START)
            child.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            child.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

            assertEquals(Lifecycle.State.DESTROYED, child.lifecycle.currentState)
        }
    }

    @Test
    fun `child follows the parent up to the cap`() = runComposeUiTest {
        runOnUiThread {
            val child = ChildLifecycleOwner()
            child.maxLifecycle = Lifecycle.State.STARTED

            child.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            child.handleLifecycleEvent(Lifecycle.Event.ON_START)
            child.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            assertEquals(Lifecycle.State.STARTED, child.lifecycle.currentState)

            child.maxLifecycle = Lifecycle.State.RESUMED
            assertEquals(Lifecycle.State.RESUMED, child.lifecycle.currentState)

            child.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            assertEquals(Lifecycle.State.STARTED, child.lifecycle.currentState)
        }
    }
}
