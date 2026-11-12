@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.enro

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
 * The content of a destination can compose in a separate composition (a Dialog window, or a
 * ModalBottomSheet layer on iOS) that is created and disposed asynchronously. That content
 * can call `navigationHandle()` or create a ViewModel for a frame after the ViewModelStore
 * of the destination is cleared. The runtime reseeds a cleared ViewModelStore with a
 * `NavigationHandleHolder` that has a no-op cleared handle (see
 * `clearAndReseedNavigationHandle`).
 *
 * `runComposeUiTest` cannot reproduce the asynchronous composition, so each test reproduces
 * one half of the race:
 *  - clearing a parent ViewModelStore while the content is composed, so that the
 *    recomposition scheduled by `NavigationHandleHolder.onCleared` reads the cleared
 *    ViewModelStore (the `ViewModelStoreStorage.onCleared` path)
 *  - looking up a ViewModel in the ViewModelStore of a destination after the destination
 *    was popped (the `clearViewModelStoreForInstance` path)
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
                            // navigationHandle() reads the handle from a mutableStateOf in the holder.
                            // Clearing the holder recomposes this content, and the recomposition
                            // looks up the holder in the cleared ViewModelStore.
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
                    // The ViewModelStore decorator stores each destination's ViewModelStore in
                    // the ViewModelStore of LocalViewModelStoreOwner. The test provides
                    // rootContext so that the test can clear that ViewModelStore.
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

            // Clearing the parent ViewModelStore clears each destination ViewModelStore through
            // ViewModelStoreStorage.onCleared and recomposes the destination content. Without
            // the reseed, the recomposition throws "No NavigationHandle found for ...".
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

            // Asserts that an operation on the cleared handle does not throw.
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
                            // navigationHandle() looks up the holder in LocalNavigationContext, so a
                            // late lookup from a separate composition uses the same owner.
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

            // A lookup after the pop must return the reseeded holder. Without the reseed, the
            // lookup throws "Expected NavigationHandleHolder to be present in ViewModelStoreOwner ...".
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
