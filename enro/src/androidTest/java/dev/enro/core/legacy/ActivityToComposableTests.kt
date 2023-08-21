@file:Suppress("DEPRECATION")
package dev.enro.core.legacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import dev.enro.*
import dev.enro.core.close
import dev.enro.destination.compose.ComposableDestination
import dev.enro.core.forward
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.*

class ActivityToComposableTests {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenActivityOpensComposable_andActivityDoesNotHaveComposeContainer_thenComposableIsLaunchedAsComposableFragmentHost() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        val id = UUID.randomUUID().toString()
        handle.forward(GenericComposableKey(id))

        // The Composable should be opened as an AbstractFragmentHostForComposable
        expectFragmentHostForComposable()
        expectContext<ComposableDestination, GenericComposableKey> {
            it.navigation.key.id == id
        }
    }

    @Test
    fun givenStandaloneComposable_whenHostFragmentCloses_thenComposableViewModelStoreIsCleared() {
        val scenario = ActivityScenario.launch(DefaultActivity::class.java)
        val handle = scenario.getNavigationHandle<DefaultActivityKey>()

        handle.forward(GenericComposableKey(id = "StandaloneComposable"))

        // The Composable should be opened as an AbstractFragmentHostForComposable
        expectFragmentHostForComposable()

        val context = expectContext<ComposableDestination, GenericComposableKey>()

        val viewModel by ViewModelLazy(
            viewModelClass = OnClearedTrackingViewModel::class,
            storeProducer = { context.context.viewModelStore },
            factoryProducer = { ViewModelProvider.NewInstanceFactory() }
        )

        assertFalse(viewModel.onClearedCalled)

        context.navigation.close()

        expectActivity<DefaultActivity>()
        waitFor { viewModel.onClearedCalled }
        assertTrue(viewModel.onClearedCalled)
    }

    @Test
    fun givenActivityHostedComposable_whenHostActivityCloses_thenComposableViewModelStoreIsCleared() {
        val scenario = ActivityScenario.launch(ActivityWithComposables::class.java)
        val handle = scenario.getNavigationHandle<ActivityWithComposablesKey>()

        handle.forward(GenericComposableKey(id = "ComposableViewModelExample"))

        val context = expectContext<ComposableDestination, GenericComposableKey>()

        val viewModel by ViewModelLazy(
            viewModelClass = OnClearedTrackingViewModel::class,
            storeProducer = { context.context.viewModelStore },
            factoryProducer = { ViewModelProvider.NewInstanceFactory() }
        )

        handle.close()

        waitFor { viewModel.onClearedCalled }
        assertTrue(viewModel.onClearedCalled)
    }
}

class OnClearedTrackingViewModel : ViewModel() {
    var onClearedCalled = false

    override fun onCleared() {
        onClearedCalled = true
    }
}