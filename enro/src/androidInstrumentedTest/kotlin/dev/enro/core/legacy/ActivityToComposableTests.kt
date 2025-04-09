@file:Suppress("DEPRECATION")
package dev.enro.core.legacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import dev.enro.DefaultActivity
import dev.enro.DefaultActivityKey
import dev.enro.GenericComposableKey
import dev.enro.core.close
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.push
import dev.enro.expectActivity
import dev.enro.expectContext
import dev.enro.expectFragmentHostForComposable
import dev.enro.getNavigationHandle
import dev.enro.waitFor
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
        handle.push(GenericComposableKey(id))

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

        handle.push(GenericComposableKey(id = "StandaloneComposable"))

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

        handle.push(GenericComposableKey(id = "ComposableViewModelExample"))

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