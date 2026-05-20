package dev.enro

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import dev.enro.controller.createNavigationModule
import dev.enro.test.EnroTest
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneDecoratorStrategy
import dev.enro.ui.SceneStrategyScope
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Compose-driven integration tests exercising the scene layer. Each test
 * installs an [EnroController], registers bindings for the test keys it
 * uses, and renders a [NavigationDisplay] inside [runComposeUiTest] —
 * which gives us fast, multiplatform-friendly tests against the real
 * runtime without needing the instrumented robot harness.
 */
@OptIn(ExperimentalTestApi::class)
class SceneIntegrationTests {

    @Test
    fun `NavigationDisplay renders the current destination's content`() = runEnroComposeTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> {
                        Text("test destination rendered")
                    }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(TestSceneKey.asInstance()),
                )
                NavigationDisplay(state = container)
            }
        }

        onNodeWithText("test destination rendered").assertIsDisplayed()
    }

    @Test
    fun `Scene strategy chain falls through to the next strategy when one returns null`() = runEnroComposeTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> {
                        Text("rendered via fall-through strategy")
                    }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        var alwaysNullCallCount = 0
        val alwaysNullStrategy = object : NavigationSceneStrategy {
            @Composable
            override fun SceneStrategyScope.calculateScene(
                entries: List<NavigationDestination<NavigationKey>>,
            ): NavigationScene? {
                alwaysNullCallCount++
                return null
            }
        }

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(TestSceneKey.asInstance()),
                )
                NavigationDisplay(
                    state = container,
                    sceneStrategy = NavigationSceneStrategy.from(
                        alwaysNullStrategy,
                        SinglePaneSceneStrategy(),
                    ),
                )
            }
        }

        assertTrue(
            actual = alwaysNullCallCount > 0,
            message = "The earlier strategy must be consulted before the chain falls through",
        )
        onNodeWithText("rendered via fall-through strategy").assertIsDisplayed()
    }

    @Test
    fun `Scene decorators are NOT applied to Overlay scenes`() = runEnroComposeTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> {
                        Text("underlying scene")
                    }
                )
                destination<OverlaySceneKey>(
                    navigationDestination<OverlaySceneKey>(
                        metadata = { directOverlay() },
                    ) {
                        Text("overlay scene")
                    }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        val decoratedScenes = mutableListOf<NavigationScene>()
        val recordingDecorator = SceneDecoratorStrategy { scene ->
            decoratedScenes += scene
            scene
        }

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(
                        TestSceneKey.asInstance(),
                        OverlaySceneKey.asInstance(),
                    ),
                )
                NavigationDisplay(
                    state = container,
                    sceneDecoratorStrategies = listOf(recordingDecorator),
                )
            }
        }

        onNodeWithText("overlay scene").assertIsDisplayed()
        assertTrue(
            actual = decoratedScenes.isNotEmpty(),
            message = "The decorator should be invoked at least once for the underlying scene",
        )
        assertFalse(
            actual = decoratedScenes.any { it is NavigationScene.Overlay },
            message = "Decorators must not be applied to NavigationScene.Overlay; decorated: $decoratedScenes",
        )
    }
}

@Serializable
data object TestSceneKey : NavigationKey

@Serializable
data object OverlaySceneKey : NavigationKey

/**
 * Wrap [runComposeUiTest] with the same install/uninstall lifecycle that
 * [dev.enro.test.runEnroTest] provides for non-Compose tests, so callers
 * can use the `ComposeUiTest` scope (setContent, onNode*, etc.) directly
 * with an installed [EnroController] available.
 */
@OptIn(ExperimentalTestApi::class)
internal fun runEnroComposeTest(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
    EnroTest.installNavigationController()
    try {
        block()
    } finally {
        EnroTest.uninstallNavigationController()
    }
}
