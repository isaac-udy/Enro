package dev.enro

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import dev.enro.ui.LocalNavigationAnimatedVisibilityScopeOrNull
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.LocalNavigationSharedTransitionScopeOrNull
import dev.enro.ui.NavigationContainerState
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
import dev.enro.ui.scenes.isDirectOverlay
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
    fun `Nested overlay scenes render content from every layer`() = runEnroComposeTest {
        // Backstack: [A, OverlayB, OverlayC]. The runtime should resolve this
        // into three composed scenes — SinglePane(A) underneath, OverlayB above
        // it, OverlayC on top — with content from each layer visible at once
        // because overlays don't replace the underlying scene chain.
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> { Text("underlying A") }
                )
                destination<OverlaySceneKey>(
                    navigationDestination<OverlaySceneKey>(
                        metadata = { directOverlay() },
                    ) { Text("overlay B") }
                )
                destination<SecondOverlaySceneKey>(
                    navigationDestination<SecondOverlaySceneKey>(
                        metadata = { directOverlay() },
                    ) { Text("overlay C") }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(
                        TestSceneKey.asInstance(),
                        OverlaySceneKey.asInstance(),
                        SecondOverlaySceneKey.asInstance(),
                    ),
                )
                NavigationDisplay(state = container)
            }
        }

        onNodeWithText("underlying A").assertIsDisplayed()
        onNodeWithText("overlay B").assertIsDisplayed()
        onNodeWithText("overlay C").assertIsDisplayed()
    }

    @Test
    fun `Overlay scenes resolve their underlying scene chain via previousEntries`() = runEnroComposeTest {
        // When an overlay is on top of the backstack, NavigationDisplay must
        // still resolve and render the scene corresponding to
        // overlay.previousEntries underneath. The decorator (which skips
        // overlays) is a convenient window into what scene was resolved for
        // that underlying chain — we can assert its entries contain exactly
        // the underlying destination.
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> { Text("underlying entry") }
                )
                destination<OverlaySceneKey>(
                    navigationDestination<OverlaySceneKey>(
                        metadata = { directOverlay() },
                    ) { Text("overlay on top") }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()
        val underlyingInstance = TestSceneKey.asInstance()
        val overlayInstance = OverlaySceneKey.asInstance()

        val decoratedScenes = mutableListOf<NavigationScene>()
        val recordingDecorator = SceneDecoratorStrategy { scene ->
            decoratedScenes += scene
            scene
        }

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(underlyingInstance, overlayInstance),
                )
                NavigationDisplay(
                    state = container,
                    sceneDecoratorStrategies = listOf(recordingDecorator),
                )
            }
        }

        onNodeWithText("overlay on top").assertIsDisplayed()

        val nonOverlayScenes = decoratedScenes.filter { it !is NavigationScene.Overlay }
        assertTrue(
            actual = nonOverlayScenes.isNotEmpty(),
            message = "An underlying scene should have been resolved and passed through the decorator",
        )
        val underlyingEntryIds = nonOverlayScenes.flatMap { it.entries.map { entry -> entry.id } }
        assertTrue(
            actual = underlyingEntryIds.contains(underlyingInstance.id),
            message = "Underlying scene's entries should include the underlying instance " +
                "(${underlyingInstance.id}); saw entry ids: $underlyingEntryIds",
        )
        assertFalse(
            actual = underlyingEntryIds.contains(overlayInstance.id),
            message = "Underlying scene must not include the overlay's own instance; " +
                "saw entry ids: $underlyingEntryIds",
        )
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun `AnimatedVisibility and SharedTransition scopes propagate into decorated scene content`() = runEnroComposeTest {
        // SceneDecoratorStrategy.decorateScene itself runs before the
        // AnimatedContent / SharedTransitionLayout that NavigationDisplay
        // sets up — but the SCENE'S CONTENT lambda returned from a decorator
        // runs inside both, so reading LocalNavigationAnimatedVisibilityScope
        // / LocalNavigationSharedTransitionScope from there should yield the
        // real scopes (with their transition state etc.). This test locks
        // that contract down.
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> { Text("animated scene content") }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        var capturedAnimatedScope: AnimatedVisibilityScope? = null
        var capturedSharedScope: SharedTransitionScope? = null
        val capturingDecorator = SceneDecoratorStrategy { scene ->
            object : NavigationScene by scene {
                override val content: @Composable () -> Unit = {
                    capturedAnimatedScope = LocalNavigationAnimatedVisibilityScopeOrNull.current
                    capturedSharedScope = LocalNavigationSharedTransitionScopeOrNull.current
                    scene.content()
                }
            }
        }

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(TestSceneKey.asInstance()),
                )
                NavigationDisplay(
                    state = container,
                    sceneDecoratorStrategies = listOf(capturingDecorator),
                )
            }
        }

        onNodeWithText("animated scene content").assertIsDisplayed()
        waitForIdle()

        val animatedScope = assertNotNull(
            capturedAnimatedScope,
            "LocalNavigationAnimatedVisibilityScope must be non-null inside decorated scene content",
        )
        assertNotNull(
            capturedSharedScope,
            "LocalNavigationSharedTransitionScope must be non-null inside decorated scene content",
        )
        assertEquals(
            expected = EnterExitState.Visible,
            actual = animatedScope.transition.targetState,
            message = "After idle, the captured scope's transition target should be Visible",
        )
    }

    @Test
    fun `Overlay onRemove suspends until completed before the scene fully leaves composition`() = runEnroComposeTest {
        // NavigationDisplay invokes scene.onRemove() in a LaunchedEffect once
        // the exit transition settles, and only calls onFullyHidden() (which
        // drops the scene from its tracking map) AFTER onRemove returns. So
        // an onRemove that suspends should keep the scene "alive" — present
        // in the rendered set — until it resolves.
        //
        // We can't observe NavigationDisplay's internal rendered map directly,
        // but we capture onRemove start / end events through the suspending
        // hook itself and assert the suspension contract that way.
        //
        // Note on "at least N" assertions: OverlaySceneRenderer initialises
        // its visibleState to false and then a LaunchedEffect flips it to
        // `true`. Between those two points there is a transient window where
        // the (transition.currentState, transition.isRunning) keys are
        // (false, false), which triggers the onRemove LaunchedEffect once on
        // initial composition. That first invocation is cancelled when the
        // visibility setter promotes the state, but its "onRemove-start"
        // event has already been recorded. We assert >=1 starts and >=1
        // ends rather than exactly-once to be robust to that initial
        // transient.
        val onRemoveSignal = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()

        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> { Text("underlying") }
                )
                destination<OverlaySceneKey>(
                    navigationDestination<OverlaySceneKey>(
                        metadata = { directOverlay() },
                    ) { Text("controlled overlay") }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        val underlyingInstance = TestSceneKey.asInstance()
        val overlayInstance = OverlaySceneKey.asInstance()
        var capturedContainer: NavigationContainerState? = null

        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(underlyingInstance, overlayInstance),
                )
                capturedContainer = container
                NavigationDisplay(
                    state = container,
                    sceneStrategy = NavigationSceneStrategy.from(
                        TestOverlayStrategy(onRemoveSignal, events),
                        SinglePaneSceneStrategy(),
                    ),
                )
            }
        }

        onNodeWithText("controlled overlay").assertIsDisplayed()

        capturedContainer!!.updateBackstack { it.dropLast(1).asBackstack() }
        waitForIdle()

        assertTrue(
            actual = events.count { it == "onRemove-start" } >= 1,
            message = "After popping the overlay, at least one onRemove invocation should have started; events: $events",
        )
        assertEquals(
            expected = 0,
            actual = events.count { it == "onRemove-end" },
            message = "No onRemove invocation should have completed while the signal is still pending; events: $events",
        )

        onRemoveSignal.complete(Unit)
        waitForIdle()

        assertTrue(
            actual = events.count { it == "onRemove-end" } >= 1,
            message = "Completing the signal should unblock at least one onRemove invocation through to its end; events: $events",
        )
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

@Serializable
data object SecondOverlaySceneKey : NavigationKey

/**
 * A scene strategy that wraps any `directOverlay()` destination in a
 * [TestOverlayScene] with a controllable [onRemove] — used by the
 * overlay-onRemove-suspension test.
 */
private class TestOverlayStrategy(
    private val onRemoveSignal: CompletableDeferred<Unit>,
    private val events: MutableList<String>,
) : NavigationSceneStrategy {
    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        val top = entries.lastOrNull() ?: return null
        if (!top.isDirectOverlay()) return null
        return TestOverlayScene(
            key = top.instance.id,
            entry = top,
            overlaidEntries = entries.dropLast(1),
            onRemoveSignal = onRemoveSignal,
            events = events,
        )
    }
}

private class TestOverlayScene(
    override val key: Any,
    val entry: NavigationDestination<NavigationKey>,
    override val overlaidEntries: List<NavigationDestination<NavigationKey>>,
    private val onRemoveSignal: CompletableDeferred<Unit>,
    private val events: MutableList<String>,
) : NavigationScene.Overlay {
    override val entries: List<NavigationDestination<NavigationKey>> = listOf(entry)
    override val previousEntries: List<NavigationDestination<NavigationKey>> = overlaidEntries
    override val content: @Composable () -> Unit = { entry.Content() }
    override suspend fun onRemove() {
        events += "onRemove-start"
        onRemoveSignal.await()
        events += "onRemove-end"
    }
}

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
