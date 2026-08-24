package dev.enro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.savedstate.savedState
import dev.enro.handle.getNavigationHandleHolder
import dev.enro.test.NavigationKeyFixtures
import dev.enro.ui.NavigationDestination
import dev.enro.ui.decorators.NavigationDestinationDecorator
import dev.enro.ui.decorators.NavigationSavedStateHolder
import dev.enro.ui.decorators.decorateNavigationDestination
import dev.enro.ui.decorators.savedStateDecorator
import dev.enro.ui.decorators.viewModelStoreDecorator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for the ViewModelStore decorator's clear-and-reseed behaviour.
 *
 * A destination's composition can outlive the clear of its ViewModelStore
 * (Dialog windows and, on iOS, ModalBottomSheet layers compose in a separate
 * composition that tears down asynchronously), so a cleared store must be
 * reseeded with a cleared NavigationHandleHolder — otherwise a late
 * `navigationHandle()` lookup fails its strict initializer and crashes.
 *
 * These tests drive the decorator directly, capture the destination-scoped
 * [ViewModelStoreOwner] a racing composition would hold, and assert that the
 * strict holder lookup still succeeds after each of the two clearing paths:
 * the destination's own pop, and a parent store clear that tears down child
 * stores through `ViewModelStoreStorage.onCleared`.
 */
@OptIn(ExperimentalTestApi::class)
class ViewModelStoreDecoratorTests {

    private class TrackingViewModel : ViewModel() {
        var isCleared = false
            private set

        override fun onCleared() {
            isCleared = true
        }
    }

    private class Harness(
        val parentStore: ViewModelStore,
        val decorator: NavigationDestinationDecorator<NavigationKey>,
        val instance: NavigationKey.Instance<NavigationKeyFixtures.SimpleKey>,
        private val getDestinationOwner: () -> ViewModelStoreOwner?,
        private val getTrackingViewModel: () -> TrackingViewModel?,
    ) {
        val destinationOwner: ViewModelStoreOwner
            get() = assertNotNull(getDestinationOwner(), "destination content did not compose")

        val trackingViewModel: TrackingViewModel
            get() = assertNotNull(getTrackingViewModel(), "destination content did not compose")
    }

    /**
     * Composes a destination wrapped in the savedState + viewModelStore
     * decorators (savedState first, as the ViewModelStore decorator requires),
     * creating a [TrackingViewModel] in the destination's store, and returns
     * the pieces a test needs to exercise the clearing paths.
     */
    private fun androidx.compose.ui.test.ComposeUiTest.composeDestination(
        isDestinationVisible: () -> Boolean = { true },
    ): Harness {
        val parentStore = ViewModelStore()
        val parentOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore get() = parentStore
        }
        val decorator = viewModelStoreDecorator(
            parentViewModelStoreOwner = parentOwner,
            viewModelStore = parentStore,
            shouldRemoveStoreOwner = { true },
        )
        val instance = NavigationKeyFixtures.SimpleKey().asInstance()

        var destinationOwner: ViewModelStoreOwner? = null
        var trackingViewModel: TrackingViewModel? = null

        val destination = decorateNavigationDestination(
            destination = NavigationDestination.createWithoutScope(instance = instance) {
                val owner = checkNotNull(LocalViewModelStoreOwner.current)
                destinationOwner = owner
                trackingViewModel = ViewModelProvider.create(
                    store = owner.viewModelStore,
                    factory = viewModelFactory { initializer { TrackingViewModel() } },
                )[TrackingViewModel::class]
            },
            decorators = listOf(
                savedStateDecorator(NavigationSavedStateHolder(savedState())),
                decorator,
            ),
        )

        setContent {
            if (isDestinationVisible()) {
                destination.Content()
            }
        }
        waitForIdle()

        return Harness(
            parentStore = parentStore,
            decorator = decorator,
            instance = instance,
            getDestinationOwner = { destinationOwner },
            getTrackingViewModel = { trackingViewModel },
        )
    }

    @Test
    fun `popping a destination clears its ViewModels and reseeds a cleared NavigationHandle`() = runComposeUiTest {
        var isDestinationVisible by mutableStateOf(true)
        val harness = composeDestination(isDestinationVisible = { isDestinationVisible })
        val destinationOwner = harness.destinationOwner
        val trackingViewModel = harness.trackingViewModel

        // Leave composition before popping, mirroring the ordering the
        // compositionTrackingDecorator produces for a real pop.
        isDestinationVisible = false
        waitForIdle()
        harness.decorator.onPop(harness.instance)

        assertTrue(trackingViewModel.isCleared)

        // A composition racing teardown still holds the destination's owner;
        // the strict holder lookup it performs must resolve the reseeded
        // holder rather than throw.
        val holder = destinationOwner.getNavigationHandleHolder()
        assertEquals(harness.instance, holder.navigationHandle.instance)
        assertEquals(Lifecycle.State.DESTROYED, holder.navigationHandle.lifecycle.currentState)
    }

    @Test
    fun `clearing the parent store clears child ViewModels and reseeds a cleared NavigationHandle`() = runComposeUiTest {
        val harness = composeDestination()
        val destinationOwner = harness.destinationOwner
        val trackingViewModel = harness.trackingViewModel

        // Clearing the parent store clears ViewModelStoreStorage, which tears
        // down every child destination store via onCleared — the path a parent
        // destination's teardown takes while children are still composing.
        harness.parentStore.clear()

        assertTrue(trackingViewModel.isCleared)

        val holder = destinationOwner.getNavigationHandleHolder()
        assertEquals(harness.instance, holder.navigationHandle.instance)
        assertEquals(Lifecycle.State.DESTROYED, holder.navigationHandle.lifecycle.currentState)
    }

    @Test
    fun `a reseeded NavigationHandle ignores operations instead of crashing`() = runComposeUiTest {
        val harness = composeDestination()
        val destinationOwner = harness.destinationOwner

        harness.parentStore.clear()

        val holder = destinationOwner.getNavigationHandleHolder()
        // The cleared handle must swallow operations (logging a warning)
        // rather than executing or throwing.
        holder.navigationHandle.execute(
            NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance()),
        )
    }
}
