package dev.enro

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.controller.createNavigationModule
import dev.enro.handle.getNavigationHandleHolder
import dev.enro.test.EnroTest
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regression tests for the "destination composition outlives its ViewModelStore" race.
 *
 * On some platforms a destination's content composes in a separate composition
 * (a Dialog window, or a ModalBottomSheet layer on iOS) that initialises and tears
 * down asynchronously from the composition that drives navigation, so content can
 * (re)compose — and call `navigationHandle()` / create ViewModels — for a frame after
 * the destination's ViewModelStore has been cleared. The runtime handles this by
 * reseeding a cleared store with a `NavigationHandleHolder` carrying a no-op cleared
 * handle (see `clearAndReseedNavigationHandle`).
 *
 * The asynchronous window/layer composition itself can't be reproduced in
 * `runComposeUiTest`, so these tests reproduce the two halves of the race
 * deterministically:
 *  - clearing a store while the content is still composed, so the recomposition
 *    scheduled by `NavigationHandleHolder.onCleared` runs against the cleared store
 *    (the `ViewModelStoreStorage.onCleared` path — a parent store being cleared);
 *  - performing a late ViewModel lookup against a destination's store after the
 *    destination was popped (the `clearViewModelStoreForInstance` path).
 */
@OptIn(ExperimentalTestApi::class)
class ClearedNavigationHandleReseedTests {

    @Test
    fun `content that recomposes after its parent ViewModelStore is cleared receives a cleared NavigationHandle`() =
        runEnroComposeTest {
            var compositionCount = 0
            var lastHandle: NavigationHandle<NavigationKey>? = null
            EnroTest.getCurrentNavigationController().addModule(
                createNavigationModule {
                    destination<ReseedTestKey>(
                        navigationDestination<ReseedTestKey> {
                            // navigationHandle() reads the holder's mutableStateOf-backed handle, so
                            // clearing the holder invalidates this scope and forces a recomposition
                            // that looks the holder up again against the (now cleared) store.
                            val handle = navigationHandle()
                            compositionCount++
                            lastHandle = handle
                            val lifecycleState by handle.lifecycle.currentStateAsState()
                            Text("state: $lifecycleState")
                        }
                    )
                }
            )
            val rootContext = NavigationContextFixtures.createRootContext()

            setContent {
                CompositionLocalProvider(
                    LocalNavigationContext provides rootContext,
                    // The ViewModelStore decorator keeps per-destination stores inside the
                    // LocalViewModelStoreOwner's store; provide the root context's so the test
                    // can clear it directly.
                    LocalViewModelStoreOwner provides rootContext,
                ) {
                    val container = rememberNavigationContainer(
                        backstack = backstackOf(ReseedTestKey.asInstance()),
                    )
                    NavigationDisplay(state = container)
                }
            }
            waitForIdle()
            val compositionsBeforeClear = compositionCount
            assertTrue(compositionsBeforeClear > 0, "destination content should have composed")

            // Clear the parent store while the destination is still composed. This clears
            // every child destination store through ViewModelStoreStorage.onCleared and
            // schedules a recomposition of the content above. Without reseeding, that
            // recomposition throws "No NavigationHandle found for ...".
            runOnUiThread { rootContext.viewModelStore.clear() }
            waitForIdle()

            assertTrue(
                actual = compositionCount > compositionsBeforeClear,
                message = "clearing the holder should have recomposed the destination content",
            )
            val handle = assertNotNull(lastHandle)
            assertEquals(Lifecycle.State.DESTROYED, handle.lifecycle.currentState)
            assertEquals(ReseedTestKey, handle.instance.key)
            onNodeWithText("state: DESTROYED").assertIsDisplayed()

            // Operations against the cleared handle are logged and ignored, never thrown.
            handle.close()
        }

    @Test
    fun `late NavigationHandleHolder lookup against a popped destination's store returns a cleared handle`() =
        runEnroComposeTest {
            var poppedDestinationOwner: ViewModelStoreOwner? = null
            var poppedHandle: NavigationHandle<NavigationKey>? = null
            EnroTest.getCurrentNavigationController().addModule(
                createNavigationModule {
                    destination<ReseedTestKey>(
                        navigationDestination<ReseedTestKey> { Text("underlying") }
                    )
                    destination<ReseedTestChildKey>(
                        navigationDestination<ReseedTestChildKey> {
                            // navigationHandle() resolves the holder through LocalNavigationContext,
                            // so this is the owner a late lookup in a racing composition would use.
                            poppedDestinationOwner = LocalNavigationContext.current
                            poppedHandle = navigationHandle()
                            Text("popped destination")
                        }
                    )
                }
            )
            val rootContext = NavigationContextFixtures.createRootContext()

            setContent {
                CompositionLocalProvider(
                    LocalNavigationContext provides rootContext,
                    LocalViewModelStoreOwner provides rootContext,
                ) {
                    val container = rememberNavigationContainer(
                        backstack = backstackOf(
                            ReseedTestKey.asInstance(),
                            ReseedTestChildKey.asInstance(),
                        ),
                    )
                    NavigationDisplay(state = container)
                }
            }
            onNodeWithText("popped destination").assertIsDisplayed()
            val owner = assertNotNull(poppedDestinationOwner)
            val handle = assertNotNull(poppedHandle)

            runOnUiThread { handle.close() }
            waitForIdle()
            onNodeWithText("underlying").assertIsDisplayed()
            assertEquals(Lifecycle.State.DESTROYED, handle.lifecycle.currentState)

            // Simulate a composition that outlived the pop creating its ViewModel late: the
            // strict lookup must find the reseeded holder rather than throwing
            // "Expected NavigationHandleHolder to be present in ViewModelStoreOwner ...".
            val lateHolder = owner.getNavigationHandleHolder()
            assertEquals(Lifecycle.State.DESTROYED, lateHolder.navigationHandle.lifecycle.currentState)
            assertEquals(ReseedTestChildKey, lateHolder.navigationHandle.instance.key)
            lateHolder.navigationHandle.close()
        }
}

@Serializable
internal data object ReseedTestKey : NavigationKey

@Serializable
internal data object ReseedTestChildKey : NavigationKey
