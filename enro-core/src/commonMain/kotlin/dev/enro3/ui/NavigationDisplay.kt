package dev.enro3.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEachReversed
import dev.enro3.EnroController
import dev.enro3.NavigationContainer
import dev.enro3.NavigationKey
import dev.enro3.NavigationOperation
import dev.enro3.ui.animation.rememberTransitionCompat
import dev.enro3.ui.decorators.decorateNavigationDestination
import dev.enro3.ui.decorators.rememberLifecycleDecorator
import dev.enro3.ui.decorators.rememberMovableContentDecorator
import dev.enro3.ui.decorators.rememberNavigationContextDecorator
import dev.enro3.ui.decorators.rememberSavedStateDecorator
import dev.enro3.ui.decorators.rememberViewModelStoreDecorator
import dev.enro3.ui.scenes.DialogSceneStrategy
import dev.enro3.ui.scenes.DirectOverlaySceneStrategy
import dev.enro3.ui.scenes.SinglePaneScene
import dev.enro3.ui.scenes.calculateSceneWithSinglePaneFallback
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * NavigationDisplay is the main composable for rendering a navigation container's content.
 * It handles:
 * - Scene management (organizing destinations into logical groups like dialogs, overlays, etc.)
 * - Transition animations between destinations
 * - Predictive back gesture support
 * - Lifecycle and state management for destinations
 * - Shared element transitions
 *
 * @param container The navigation container whose backstack will be displayed
 * @param modifier Modifier to be applied to the root content
 * @param sceneStrategy Strategy for organizing destinations into scenes (e.g., dialogs, overlays, single pane)
 * @param contentAlignment Alignment of content within the display
 * @param sizeTransform Transform to apply when content size changes during transitions
 * @param transitionSpec Animation spec for forward navigation transitions
 * @param popTransitionSpec Animation spec for back navigation transitions
 * @param predictivePopTransitionSpec Animation spec for predictive back gesture transitions
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
public fun NavigationDisplay(
    container: NavigationContainer,
    modifier: Modifier = Modifier,
    sceneStrategy: NavigationSceneStrategy = remember {
        NavigationSceneStrategy.from(
            DialogSceneStrategy(),
            DirectOverlaySceneStrategy(),
            SinglePaneScene(),
        )
    },
    contentAlignment: Alignment = Alignment.TopStart,
    sizeTransform: SizeTransform? = null,
    transitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        ContentTransform(
            targetContentEnter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + slideInHorizontally { it / 3 },
            initialContentExit = slideOutHorizontally { -it / 4 },
        )
    },
    popTransitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        ContentTransform(
            targetContentEnter = slideInHorizontally { -it / 4 },
            initialContentExit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutHorizontally { it / 3 },
        )
    },
    predictivePopTransitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = popTransitionSpec,
) {
    val controller = rememberEnroController()
    val backstack = container.backstack.collectAsState().value
    require(backstack.isNotEmpty()) { "NavigationDisplay backstack cannot be empty" }

    // Create and remember the state that tracks the display's internal state
    val state = remember { NavigationDisplayState() }

    // Convert the raw backstack entries into decorated NavigationDestination objects
    val destinations = rememberDecoratedDestinations(controller, backstack, state.isSettled)

    // Calculate the scene hierarchy - scenes organize destinations into logical groups
    val (scene, overlayScenes) = calculateScenes(
        destinations = destinations,
        sceneStrategy = sceneStrategy,
        onBack = { count -> container.execute(NavigationOperation.closeByCount(count)) }
    )

    // Create a unique key for the current scene and store it
    val sceneKey = SceneKey(
        scene::class,
        scene.key
    )
    state.scenes[sceneKey] = scene

    // Set up predictive back gesture handling
    HandlePredictiveBack(
        scene = scene,
        destinations = destinations,
        state = state,
        onBack = { count -> container.execute(NavigationOperation.closeByCount(count)) }
    )

    // Create the transition state that manages animations between scenes
    val transitionState = remember { SeekableTransitionState(sceneKey) }
    val transition = rememberTransitionCompat(transitionState, label = sceneKey.toString())

    // Calculate which destinations should be rendered in each scene
    val sceneToRenderableDestinationMap = calculateSceneToRenderableDestinationMap(
        state = state,
        transition = transition,
    )

    // Determine if this is a pop (back) navigation
    val isPop = isPop(
        remember(transition.currentState) { destinations.toList() }.map { it.instance.id },
        destinations.map { it.instance.id }
    )

    // Calculate z-indices for proper layering during transitions
    val zIndices = updateZIndices(
        transition = transition,
        isPop = isPop,
        inPredictiveBack = state.inPredictiveBack
    )

    // Handle transition animations
    TransitionAnimationEffect(
        transitionState = transitionState,
        transition = transition,
        sceneKey = sceneKey,
        progress = state.progress,
        inPredictiveBack = state.inPredictiveBack,
        scene = scene,
        sceneStrategy = sceneStrategy,
        scenes = state.scenes
    )

    // Select the appropriate transition spec based on navigation type
    val contentTransform: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        when {
            state.inPredictiveBack -> predictivePopTransitionSpec(this)
            isPop -> popTransitionSpec(this)
            else -> transitionSpec(this)
        }
    }

    // Render the navigation content
    CompositionLocalProvider(LocalNavigationContainer provides container) {
        RenderMainContent(
            transition = transition,
            scenes = state.scenes,
            sceneToRenderableDestinationMap = sceneToRenderableDestinationMap,
            zIndices = zIndices,
            contentTransform = contentTransform,
            contentAlignment = contentAlignment,
            modifier = modifier,
            sizeTransform = sizeTransform
        )
        CleanupSceneEffect(transition, state)
        UpdateSettledStateEffect(transition) { state.isSettled = it }
        RenderOverlayScenes(overlayScenes)
    }
}

/**
 * Internal state management for NavigationDisplay.
 * Tracks the current state of animations, scenes, and navigation gestures.
 */
private class NavigationDisplayState {
    /** Progress of the current predictive back gesture (0.0 to 1.0) */
    var progress by mutableFloatStateOf(0f)

    /** Whether a predictive back gesture is currently in progress */
    var inPredictiveBack by mutableStateOf(false)

    /** Whether the navigation state is settled (no animations running) */
    var isSettled by mutableStateOf(true)

    /** Map of scene keys to their corresponding NavigationScene instances */
    val scenes = mutableStateMapOf<SceneKey, NavigationScene>()

    /** Ordered list of scene keys, with most recently targeted scenes last */
    val mostRecentSceneKeys = mutableStateListOf<SceneKey>()
}

/**
 * Retrieves and remembers the EnroController instance.
 * The controller manages navigation bindings and destination creation.
 */
@Composable
private fun rememberEnroController(): EnroController {
    return remember {
        requireNotNull(EnroController.instance) {
            "EnroController must be initialized before using NavigationDisplay"
        }
    }
}

/**
 * Creates NavigationDestination instances from the backstack and applies decorators.
 * Decorators add functionality like lifecycle management, view models, and saved state.
 *
 * @param controller The navigation controller for binding resolution
 * @param backstack The current navigation backstack
 * @param isSettled Whether animations are currently settled (used by lifecycle decorator)
 * @return List of decorated NavigationDestination instances
 */
@Composable
private fun rememberDecoratedDestinations(
    controller: EnroController,
    backstack: List<NavigationKey.Instance<*>>,
    isSettled: Boolean,
): List<NavigationDestination<NavigationKey>> {
    // Create decorators that wrap destinations with additional functionality
    val decorators = listOf(
        rememberMovableContentDecorator(),  // Preserves content across recompositions
        rememberSavedStateDecorator(),      // Manages saved instance state
        rememberViewModelStoreDecorator(),  // Provides ViewModelStore for each destination
        rememberLifecycleDecorator(backstack, isSettled),  // Manages lifecycle state
        rememberNavigationContextDecorator(),  // Provides navigation context
    )

    return remember(backstack) {
        backstack
            .map { instance ->
                // Find the navigation binding for this instance and create the destination
                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<NavigationKey>
                val binding = controller.bindings.bindingFor(instance)
                binding.provider.create(instance)
            }
            .map {
                // Apply all decorators to enhance the destination
                decorateNavigationDestination(
                    destination = it,
                    decorators = decorators,
                )
            }
    }
}

/**
 * Calculates the scene hierarchy from the list of destinations.
 * Scenes organize destinations into logical groups (e.g., main content, dialogs, overlays).
 *
 * The function recursively processes overlay scenes, building a hierarchy where:
 * - The main scene contains the primary content
 * - Overlay scenes (like dialogs) are rendered on top
 * - Each overlay scene may itself have overlaid content
 *
 * @param destinations The list of all destinations to organize into scenes
 * @param sceneStrategy The strategy for determining scene organization
 * @param onBack Callback for handling back navigation
 * @return Pair of the main scene and list of overlay scenes
 */
@Composable
private fun calculateScenes(
    destinations: List<NavigationDestination<NavigationKey>>,
    sceneStrategy: NavigationSceneStrategy,
    onBack: (Int) -> Unit,
): Pair<NavigationScene, List<NavigationScene.Overlay>> {
    // Start with calculating the first scene from all destinations
    val allScenes = mutableListOf(sceneStrategy.calculateSceneWithSinglePaneFallback(destinations, onBack))
    var currentScene = allScenes.last()

    // Process overlay scenes recursively
    // Each overlay scene may have destinations that should be overlaid on it
    while (currentScene is NavigationScene.Overlay && currentScene.overlaidEntries.isNotEmpty()) {
        allScenes += sceneStrategy.calculateSceneWithSinglePaneFallback(currentScene.overlaidEntries, onBack)
        currentScene = allScenes.last()
    }

    // The last scene is the main scene, all others are overlays
    val overlayScenes = allScenes.dropLast(1).filterIsInstance<NavigationScene.Overlay>()
    val scene = allScenes.last()
    return scene to overlayScenes
}

/**
 * Sets up handling for predictive back gestures.
 * Monitors back gesture events and updates the state accordingly.
 *
 * @param scene The current scene
 * @param destinations All destinations in the backstack
 * @param state The navigation display state to update
 * @param onBack Callback to execute when back gesture completes
 */
@Composable
private fun HandlePredictiveBack(
    scene: NavigationScene,
    destinations: List<NavigationDestination<NavigationKey>>,
    state: NavigationDisplayState,
    onBack: (Int) -> Unit,
) {
    NavigationBackHandler(scene.previousEntries.isNotEmpty()) { navEvent ->
        state.progress = 0f
        try {
            // Collect gesture progress events
            navEvent.collect { value ->
                state.inPredictiveBack = true
                state.progress = value.progress
            }
            // Gesture completed - execute the back navigation
            state.inPredictiveBack = false
            onBack(destinations.size - scene.previousEntries.size)
        } finally {
            // Ensure state is cleaned up even if an error occurs
            state.inPredictiveBack = false
        }
    }
}

/**
 * A key that uniquely identifies a scene instance.
 * Combines the scene's type (KClass) with its instance key.
 */
private data class SceneKey(
    val sceneType: KClass<*>,
    val key: Any,
)

/**
 * Calculates which destinations should be rendered in each scene.
 *
 * This function ensures that:
 * - Each destination is only rendered in one scene
 * - More recently targeted scenes get priority for rendering destinations
 * - The transition target scene always renders its destinations
 *
 * @param state The navigation display state containing scene information
 * @param transition The current transition state
 * @return Map of scene keys to sets of destination IDs that should be rendered in each scene
 */
@Composable
private fun calculateSceneToRenderableDestinationMap(
    state: NavigationDisplayState,
    transition: Transition<SceneKey>,
): Map<SceneKey, Set<String>> {
    // Update the most recent scene keys list when target changes
    LaunchedEffect(transition.targetState) {
        if (state.mostRecentSceneKeys.lastOrNull() != transition.targetState) {
            state.mostRecentSceneKeys.remove(transition.targetState)
            state.mostRecentSceneKeys.add(transition.targetState)
        }
    }

    return remember(
        state.mostRecentSceneKeys,
        state.scenes.values.map { scene -> scene.entries.map { it.instance.id } },
        transition.targetState,
    ) {
        buildMap {
            val coveredDestinationIds = mutableSetOf<String>()
            // Process scenes from most recent to least recent
            (state.mostRecentSceneKeys.filter { it != transition.targetState } + listOf(transition.targetState))
                .fastForEachReversed { sceneKey ->
                    val scene = state.scenes.getValue(sceneKey)
                    put(
                        sceneKey,
                        scene.entries
                            .map { it.instance.id }
                            .filterNot(coveredDestinationIds::contains)  // Only include uncovered destinations
                            .toSet(),
                    )
                    // Mark these destinations as covered
                    scene.entries.forEach { coveredDestinationIds.add(it.instance.id) }
                }
        }
    }
}

/**
 * Manages z-indices for proper layering during transitions.
 *
 * Z-index logic:
 * - Forward navigation: new content appears on top (higher z-index)
 * - Back navigation: old content appears on top (lower z-index for new content)
 * - No change when transitioning to the same scene
 *
 * @param transition The current transition state
 * @param isPop Whether this is a back navigation
 * @param inPredictiveBack Whether a predictive back gesture is active
 * @return Map of scene keys to their z-indices
 */
@Composable
private fun updateZIndices(
    transition: Transition<SceneKey>,
    isPop: Boolean,
    inPredictiveBack: Boolean,
): MutableMap<SceneKey, Float> {
    val zIndices = remember { mutableMapOf<SceneKey, Float>() }
    val initialKey = transition.currentState
    val targetKey = transition.targetState
    val initialZIndex = zIndices.getOrPut(initialKey) { 0f }

    // Calculate target z-index based on navigation direction
    val targetZIndex = when {
        initialKey == targetKey -> initialZIndex  // No change for same scene
        isPop || inPredictiveBack -> initialZIndex - 1f  // Lower for back navigation
        else -> initialZIndex + 1f  // Higher for forward navigation
    }
    zIndices[targetKey] = targetZIndex
    return zIndices
}

/**
 * Manages transition animations between scenes.
 *
 * Handles two cases:
 * 1. Predictive back: Creates a peek scene and animates based on gesture progress
 * 2. Regular navigation: Animates to the target scene or handles settling animations
 *
 * @param transitionState The seekable transition state for controlling animations
 * @param transition The current transition
 * @param sceneKey The target scene key
 * @param progress Current progress of predictive back gesture
 * @param inPredictiveBack Whether predictive back is active
 * @param scene The current scene
 * @param sceneStrategy Strategy for calculating scenes
 * @param scenes Map of all scenes
 */
@Composable
private fun TransitionAnimationEffect(
    transitionState: SeekableTransitionState<SceneKey>,
    transition: Transition<SceneKey>,
    sceneKey: SceneKey,
    progress: Float,
    inPredictiveBack: Boolean,
    scene: NavigationScene,
    sceneStrategy: NavigationSceneStrategy,
    scenes: MutableMap<SceneKey, NavigationScene>,
) {
    if (inPredictiveBack) {
        // During predictive back, create a "peek" scene showing the previous destinations
        val peekScene = sceneStrategy.calculateSceneWithSinglePaneFallback(
            scene.previousEntries,
            { /* No-op during predictive back */ }
        )
        val peekSceneKey = SceneKey(
            sceneType = peekScene::class,
            key = peekScene.key,
        )
        scenes[peekSceneKey] = peekScene

        // Seek to the appropriate position based on gesture progress
        if (transitionState.currentState != peekSceneKey) {
            LaunchedEffect(progress) { transitionState.seekTo(progress, peekSceneKey) }
        }
    } else {
        // Regular navigation - animate to target or handle settling
        LaunchedEffect(sceneKey) {
            if (transitionState.currentState != sceneKey) {
                // Animate to the new scene
                transitionState.animateTo(sceneKey)
            } else {
                // Already at target - animate any remaining settling
                val totalDuration = transition.totalDurationNanos / 1000000
                animate(
                    transitionState.fraction,
                    0f,
                    animationSpec = tween((transitionState.fraction * totalDuration).toInt()),
                ) { value, _ ->
                    this@LaunchedEffect.launch {
                        if (value > 0) {
                            transitionState.seekTo(value)
                        }
                        if (value == 0f) {
                            transitionState.snapTo(sceneKey)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the main navigation content with animated transitions.
 *
 * Uses SharedTransitionLayout and AnimatedContent to:
 * - Animate between different scenes
 * - Support shared element transitions
 * - Provide proper scoping for animations
 *
 * @param transition The current transition state
 * @param scenes Map of all available scenes
 * @param sceneToRenderableDestinationMap Map of which destinations to render in each scene
 * @param zIndices Z-index values for each scene
 * @param contentTransform Transform to apply during transitions
 * @param contentAlignment Alignment of content
 * @param modifier Modifier for the content
 * @param sizeTransform Transform for size changes
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RenderMainContent(
    transition: Transition<SceneKey>,
    scenes: Map<SceneKey, NavigationScene>,
    sceneToRenderableDestinationMap: Map<SceneKey, Set<String>>,
    zIndices: Map<SceneKey, Float>,
    contentTransform: AnimatedContentTransitionScope<*>.() -> ContentTransform,
    contentAlignment: Alignment,
    modifier: Modifier,
    sizeTransform: SizeTransform?,
) {
    SharedTransitionLayout {
        transition.AnimatedContent(
            contentAlignment = contentAlignment,
            modifier = modifier,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = contentTransform(this).targetContentEnter,
                    initialContentExit = contentTransform(this).initialContentExit,
                    targetContentZIndex = zIndices[transition.targetState] ?: 0f,
                    sizeTransform = sizeTransform,
                )
            }
        ) { targetSceneKey ->
            val targetScene = scenes.getValue(targetSceneKey)
            // Provide necessary composition locals for the scene content
            CompositionLocalProvider(
                LocalNavigationAnimatedVisibilityScope provides this@AnimatedContent,
                LocalNavigationSharedTransitionScope provides this@SharedTransitionLayout,
                LocalDestinationsToRenderInCurrentScene provides sceneToRenderableDestinationMap.getValue(
                    targetSceneKey
                )
            ) {
                targetScene.content()
            }
        }
    }
}

/**
 * Cleans up scenes that are no longer needed after transitions complete.
 * This prevents memory leaks and ensures only active scenes are retained.
 *
 * @param transition The current transition state
 * @param state The navigation display state containing scenes
 */
@Composable
private fun CleanupSceneEffect(
    transition: Transition<SceneKey>,
    state: NavigationDisplayState,
) {
    LaunchedEffect(transition) {
        snapshotFlow { transition.isRunning }
            .filter { !it }  // Only proceed when transition is complete
            .collect {
                // Remove all scenes except the current target
                state.scenes.keys.toList().forEach { key ->
                    if (key != transition.targetState) {
                        state.scenes.remove(key)
                    }
                }
                // Clean up the most recent keys list as well
                state.mostRecentSceneKeys.toList().forEach { key ->
                    if (key != transition.targetState) {
                        state.mostRecentSceneKeys.remove(key)
                    }
                }
            }
    }
}

/**
 * Updates the settled state based on transition progress.
 * The state is settled when no transition is running (current == target).
 *
 * @param transition The current transition state
 * @param onSettledChange Callback invoked when settled state changes
 */
@Composable
private fun UpdateSettledStateEffect(
    transition: Transition<SceneKey>,
    onSettledChange: (Boolean) -> Unit,
) {
    LaunchedEffect(transition.currentState, transition.targetState) {
        val settled = transition.currentState == transition.targetState
        onSettledChange(settled)
    }
}

/**
 * Renders overlay scenes (like dialogs) on top of the main content.
 * Each overlay gets its own SharedTransitionLayout for independent animations.
 *
 * @param overlayScenes List of overlay scenes to render
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RenderOverlayScenes(overlayScenes: List<NavigationScene.Overlay>) {
    overlayScenes.fastForEachReversed { overlayScene ->
        val allDestinations = overlayScene.entries.map { it.instance.id }.toSet()
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                CompositionLocalProvider(
                    LocalNavigationAnimatedVisibilityScope provides this@AnimatedVisibility,
                    LocalNavigationSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalDestinationsToRenderInCurrentScene provides allDestinations
                ) {
                    overlayScene.content()
                }
            }
        }
    }
}

/**
 * Determines if a navigation operation is a "pop" (back navigation).
 *
 * A pop is detected when:
 * - The backstacks share the same root
 * - The new backstack is shorter than the old one
 * - The new backstack is a prefix of the old backstack
 *
 * @param oldBackStack The previous backstack state
 * @param newBackStack The new backstack state
 * @return true if this is a back navigation, false otherwise
 */
private fun <T : Any> isPop(oldBackStack: List<T>, newBackStack: List<T>): Boolean {
    if (oldBackStack.isEmpty() || newBackStack.isEmpty()) return false
    if (oldBackStack.first() != newBackStack.first()) return false  // Different roots
    if (newBackStack.size > oldBackStack.size) return false  // Can't be pop if growing

    // Check if new backstack is a prefix of the old one
    val divergingIndex = newBackStack.indices.firstOrNull { index ->
        newBackStack[index] != oldBackStack[index]
    }
    return divergingIndex == null && newBackStack.size != oldBackStack.size
}
