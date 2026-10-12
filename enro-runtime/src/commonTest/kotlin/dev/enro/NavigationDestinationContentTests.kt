@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.enro

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import dev.enro.controller.createNavigationModule
import dev.enro.test.EnroTest
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.contains
import dev.enro.ui.decorators.LocalNavigationDestinationWrapper
import dev.enro.ui.decorators.NavigationDestinationWrapper
import dev.enro.ui.get
import dev.enro.ui.navigationDestination
import dev.enro.ui.navigationDestinationContent
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.OverlayTransitionsKey
import dev.enro.ui.scenes.directOverlay
import dev.enro.ui.scenes.directOverlayWithFade
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class NavigationDestinationContentTests {

    @Test
    fun `navigationDestinationContent renders with the destination's navigation handle and metadata`() = runEnroComposeTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<ContentScopeTestKey>(
                    navigationDestinationContent<ContentScopeTestKey>(
                        metadata = { add("label-suffix", "with metadata") },
                    ) {
                        Text("${navigation.key.label} ${destinationMetadata["label-suffix"]}")
                    }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(ContentScopeTestKey("content").asInstance()),
                )
                NavigationDisplay(state = container)
            }
        }

        onNodeWithText("content with metadata").assertIsDisplayed()
    }

    @Test
    fun `NavigationDisplay provides a destination wrapper`() = runEnroComposeTest {
        var wrapper: NavigationDestinationWrapper? = null
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<ContentScopeTestKey>(
                    navigationDestination<ContentScopeTestKey> {
                        wrapper = LocalNavigationDestinationWrapper.current
                        Text(navigation.key.label)
                    }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(ContentScopeTestKey("wrapped").asInstance()),
                )
                NavigationDisplay(state = container)
            }
        }

        onNodeWithText("wrapped").assertIsDisplayed()
        assertNotNull(wrapper)
    }

    @Test
    fun `directOverlayWithFade stores the fade duration instead of Compose transitions`() {
        val metadata = navigationDestinationContent<ContentScopeTestKey>(
            metadata = { directOverlayWithFade(durationMillis = 200) },
        ) {}.peekMetadata(ContentScopeTestKey("fade").asInstance())

        assertTrue(metadata[DirectOverlaySceneStrategy.IsDirectOverlayKey])
        assertEquals(200, metadata[DirectOverlaySceneStrategy.OverlayFadeKey])
        assertFalse(OverlayTransitionsKey in metadata)
    }

    @Test
    fun `directOverlay with transitions stores them under OverlayTransitionsKey`() {
        val enter = fadeIn()
        val exit = fadeOut()
        val metadata = navigationDestination<ContentScopeTestKey>(
            metadata = { directOverlay(enter = enter, exit = exit) },
        ) {}.peekMetadata(ContentScopeTestKey("transitions").asInstance())

        assertTrue(metadata[DirectOverlaySceneStrategy.IsDirectOverlayKey])
        val transitions = assertNotNull(metadata[OverlayTransitionsKey])
        assertEquals(enter, transitions.enter)
        assertEquals(exit, transitions.exit)
        assertFalse(DirectOverlaySceneStrategy.OverlayFadeKey in metadata)
    }

    @Test
    fun `A faded overlay leaves composition after it is closed`() = runEnroComposeTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestinationContent<TestSceneKey> { Text("underlying") }
                )
                destination<OverlaySceneKey>(
                    navigationDestinationContent<OverlaySceneKey>(
                        metadata = { directOverlayWithFade(durationMillis = 50) },
                    ) { Text("faded overlay") }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()
        var container: NavigationContainerState? = null

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                container = rememberNavigationContainer(
                    backstack = backstackOf(TestSceneKey.asInstance(), OverlaySceneKey.asInstance()),
                )
                NavigationDisplay(state = container!!)
            }
        }

        onNodeWithText("faded overlay").assertIsDisplayed()

        container!!.updateBackstack { it.dropLast(1).asBackstack() }
        waitForIdle()

        onNodeWithText("faded overlay").assertDoesNotExist()
        onNodeWithText("underlying").assertIsDisplayed()
    }
}

@Serializable
data class ContentScopeTestKey(val label: String) : NavigationKey
