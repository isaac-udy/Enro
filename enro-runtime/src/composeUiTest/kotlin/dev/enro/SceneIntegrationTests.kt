@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.enro

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.enro.controller.createNavigationModule
import dev.enro.test.EnroTest
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.ui.LocalNavigationAnimatedVisibilityScopeOrNull
import dev.enro.ui.LocalNavigationContainer
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
import dev.enro.ui.scenes.directOverlayWithFade
import dev.enro.ui.scenes.isDirectOverlay
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        // Scene decorators are not applied to overlays, so the scenes recorded by the decorator
        // are the scenes that NavigationDisplay resolved under the overlay.
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
        // decorateScene runs outside the AnimatedContent and SharedTransitionLayout of
        // NavigationDisplay. The content lambda of the decorated scene runs inside both, so the
        // decorator reads the scopes in the content lambda.
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
        // The test cannot read the scenes that NavigationDisplay renders, so
        // TestOverlayScene.onRemove records the start and the end of onRemove in `events`.
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
        assertEquals(
            expected = emptyList<String>(),
            actual = events.toList(),
            message = "onRemove must not fire while the overlay is on the backstack",
        )

        capturedContainer!!.updateBackstack { it.dropLast(1).asBackstack() }
        waitForIdle()

        assertEquals(
            expected = listOf("onRemove-start"),
            actual = events.toList(),
            message = "After popping the overlay, onRemove should have started exactly once and be suspended on the signal",
        )

        onRemoveSignal.complete(Unit)
        waitForIdle()

        assertEquals(
            expected = listOf("onRemove-start", "onRemove-end"),
            actual = events.toList(),
            message = "Completing the signal should resume the suspended onRemove through to its end",
        )
    }

    @Test
    fun `Overlay content stays composed while onRemove is suspended`() = runEnroComposeTest {
        val onRemoveSignal = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()
        val container = setSuspendingOverlayContent(onRemoveSignal, events) { directOverlay() }

        onNodeWithTag(OverlayContentTag).assertIsDisplayed()

        container.updateBackstack { it.dropLast(1).asBackstack() }
        waitForIdle()

        assertEquals(listOf("onRemove-start"), events.toList())
        onNodeWithTag(OverlayContentTag).assertIsDisplayed()

        onRemoveSignal.complete(Unit)
        waitForIdle()

        assertEquals(listOf("onRemove-start", "onRemove-end"), events.toList())
        onNodeWithTag(OverlayContentTag).assertDoesNotExist()
    }

    @Test
    fun `A faded overlay runs onRemove before its exit transition`() = runEnroComposeTest {
        val onRemoveSignal = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()
        val container = setSuspendingOverlayContent(onRemoveSignal, events) {
            directOverlayWithFade(durationMillis = 400)
        }

        onNodeWithTag(OverlayContentTag).assertIsDisplayed()

        container.updateBackstack { it.dropLast(1).asBackstack() }
        waitForIdle()

        assertEquals(listOf("onRemove-start"), events.toList())
        onNodeWithTag(OverlayContentTag).assertIsDisplayed()

        mainClock.autoAdvance = false
        onRemoveSignal.complete(Unit)
        mainClock.advanceTimeBy(200)

        assertEquals(listOf("onRemove-start", "onRemove-end"), events.toList())
        onNodeWithTag(OverlayContentTag).assertExists()

        mainClock.advanceTimeBy(400)
        onNodeWithTag(OverlayContentTag).assertDoesNotExist()
    }

    @Test
    fun `Re-adding an overlay while onRemove is suspended cancels the removal and keeps its state`() = runEnroComposeTest {
        val onRemoveSignal = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()
        val container = setSuspendingOverlayContent(onRemoveSignal, events) { directOverlay() }
        val backstack = container.backstack

        onNodeWithTag(OverlayContentTag).performClick()
        onNodeWithText("clicked 1").assertIsDisplayed()

        container.updateBackstack { it.dropLast(1).asBackstack() }
        waitForIdle()
        assertEquals(listOf("onRemove-start"), events.toList())

        container.updateBackstack { backstack }
        waitForIdle()
        onRemoveSignal.complete(Unit)
        waitForIdle()

        assertEquals(
            expected = listOf("onRemove-start"),
            actual = events.toList(),
            message = "Re-adding the overlay should cancel the suspended onRemove",
        )
        onNodeWithText("clicked 1").assertIsDisplayed()
    }

    @Test
    fun `requestClose routes through onCloseRequested callback when one is registered in an overlay destination`() = runEnroComposeTest {
        var callbackInvocations = 0

        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> { Text("underlying") }
                )
                destination<OverlaySceneKey>(
                    navigationDestination<OverlaySceneKey>(
                        metadata = { directOverlay() },
                    ) {
                        navigation.configure {
                            onCloseRequested {
                                callbackInvocations++
                                // The callback does not call close(), so the overlay must stay open.
                            }
                        }
                        Button(onClick = { navigation.requestClose() }) {
                            Text("dismiss overlay")
                        }
                    }
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
                    ),
                )
                NavigationDisplay(state = container)
            }
        }

        onNodeWithText("dismiss overlay").assertIsDisplayed()
        assertEquals(0, callbackInvocations)

        onNodeWithText("dismiss overlay").performClick()
        waitForIdle()

        assertEquals(
            expected = 1,
            actual = callbackInvocations,
            message = "onCloseRequested callback should have fired exactly once after requestClose",
        )
        onNodeWithText("dismiss overlay").assertIsDisplayed()
    }

    @Test
    fun `LocalNavigationContainer and LocalNavigationContext are readable inside decorateScene`() = runEnroComposeTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<TestSceneKey>(
                    navigationDestination<TestSceneKey> { Text("decorated content") }
                )
            }
        )
        val rootContext = NavigationContextFixtures.createRootContext()

        var capturedContainer: NavigationContainerState? = null
        var capturedContext: dev.enro.NavigationContext? = null
        val capturingDecorator = SceneDecoratorStrategy { scene ->
            capturedContainer = LocalNavigationContainer.current
            capturedContext = LocalNavigationContext.current
            scene
        }

        var expectedContainer: NavigationContainerState? = null
        setContent {
            CompositionLocalProvider(LocalNavigationContext provides rootContext) {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(TestSceneKey.asInstance()),
                )
                expectedContainer = container
                NavigationDisplay(
                    state = container,
                    sceneDecoratorStrategies = listOf(capturingDecorator),
                )
            }
        }

        onNodeWithText("decorated content").assertIsDisplayed()
        waitForIdle()

        val seenContainer = assertNotNull(
            capturedContainer,
            "LocalNavigationContainer.current must resolve inside decorateScene",
        )
        assertEquals(
            expected = expectedContainer,
            actual = seenContainer,
            message = "Decorator should see the same NavigationContainerState that NavigationDisplay was given",
        )
        assertEquals(
            expected = expectedContainer!!.context,
            actual = capturedContext,
            message = "Decorator should see the container's own context via LocalNavigationContext, not a fallback root context",
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

private const val OverlayContentTag = "overlay-content"

/**
 * Renders [TestSceneKey] under an [OverlaySceneKey] shown by [TestOverlayStrategy]. The overlay
 * is a button tagged [OverlayContentTag] that counts its clicks in remembered state.
 */
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setSuspendingOverlayContent(
    onRemoveSignal: CompletableDeferred<Unit>,
    events: MutableList<String>,
    overlayMetadata: NavigationDestination.MetadataBuilder<OverlaySceneKey>.() -> Unit,
): NavigationContainerState {
    EnroTest.getCurrentNavigationController().addModule(
        createNavigationModule {
            destination<TestSceneKey>(
                navigationDestination<TestSceneKey> { Text("underlying") }
            )
            destination<OverlaySceneKey>(
                navigationDestination<OverlaySceneKey>(metadata = overlayMetadata) {
                    var clicks by remember { mutableIntStateOf(0) }
                    Button(
                        onClick = { clicks++ },
                        modifier = Modifier.testTag(OverlayContentTag),
                    ) {
                        Text("clicked $clicks")
                    }
                }
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
            NavigationDisplay(
                state = container!!,
                sceneStrategy = NavigationSceneStrategy.from(
                    TestOverlayStrategy(onRemoveSignal, events),
                    SinglePaneSceneStrategy(),
                ),
            )
        }
    }
    return container!!
}

@Serializable
data object TestSceneKey : NavigationKey

@Serializable
data object OverlaySceneKey : NavigationKey

@Serializable
data object SecondOverlaySceneKey : NavigationKey

/**
 * Wraps a `directOverlay()` destination in a [TestOverlayScene]. `TestOverlayScene.onRemove`
 * suspends until `onRemoveSignal` completes and records the start and the end in `events`.
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
 * Runs [block] in [runComposeUiTest] with an [EnroController] installed, as
 * [dev.enro.test.runEnroTest] does for a test without Compose.
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
