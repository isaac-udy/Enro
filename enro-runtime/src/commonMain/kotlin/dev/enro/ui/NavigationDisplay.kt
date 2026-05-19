package dev.enro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.platform.EnroLog
import dev.enro.requestClose
import dev.enro.ui.decorators.ProvideRemovalTrackingInfo
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.OverlayTransitions
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import dev.enro.ui.scenes.calculateSceneWithSinglePaneFallback
import dev.enro.viewmodel.getNavigationHandle
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * NavigationDisplay is the main composable for rendering a navigation container's content.
 *
 * It renders and animates between different [NavigationScene]s, each of which can render one
 * or more [NavigationDestination]s. Overlay scenes (like dialogs) are rendered on top.
 *
 * The [NavigationScene]s are calculated with the given [NavigationSceneStrategy], which may be
 * an assembled chain of strategies. If no scene is calculated, the fallback will be to a
 * [SinglePaneSceneStrategy].
 *
 * It is allowable for different scenes to render the same entries, perhaps on some conditions
 * as determined by the [sceneStrategy] based on window size, form factor, or other logic.
 *
 * If this happens, and these scenes are rendered at the same time due to animation or predictive
 * back, then the content for the entry will only be rendered in the most recent scene that
 * is the target for being the current scene. This enforces a unique invocation of each entry,
 * even if it is displayable by two different scenes.
 *
 * @param state The navigation container state whose backstack will be displayed
 * @param modifier Modifier to be applied to the root content
 * @param sceneStrategy Strategy for organizing destinations into scenes (e.g., dialogs, overlays, single pane)
 * @param contentAlignment Alignment of content within the display
 * @param sizeTransform Transform to apply when content size changes during transitions
 * @param animations Animation specs for navigation transitions
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
public fun NavigationDisplay(
    state: NavigationContainerState,
    modifier: Modifier = Modifier,
    sceneStrategy: NavigationSceneStrategy = remember {
        NavigationSceneStrategy.from(
            DialogSceneStrategy(),
            DirectOverlaySceneStrategy(),
            SinglePaneSceneStrategy(),
        )
    },
    contentAlignment: Alignment = Alignment.TopStart,
    sizeTransform: SizeTransform? = null,
    animations: NavigationAnimations = NavigationAnimations.Default,
) {
    DisposableEffect(state) {
        state.context.parent.registerVisibility(state.context, true)
        onDispose {
            state.context.parent.registerVisibility(state.context, false)
        }
    }

    // Calculate the scene hierarchy - scenes organize destinations into logical groups
    val (scene, overlayScenes) = calculateScenes(
        state = state,
        sceneStrategy = sceneStrategy,
    )

    // The SceneState wraps the scene with the data needed by transition
    // specs (containerKey/visible/previouslyVisible). It is what flows
    // through SeekableTransitionState, but AnimatedContent's `contentKey`
    // collapses it down to a SceneIdentity, so two SceneStates that
    // share a (sceneType, scene.key, containerKey) reuse the same
    // AnimatedContent slot and do NOT trigger a scene-level enter/exit
    // animation. This mirrors Nav3's AnimatedSceneKey indirection and
    // lets the scene strategy author control re-animation through
    // `scene.key` alone.
    val sceneState = SceneState(scene, state.container.key)
    val sceneIdentity = sceneState.identity

    // Scene tracking maps (like NavDisplay's sceneMap and zIndices)
    val sceneMap = remember { mutableStateMapOf<SceneIdentity, NavigationScene>() }
    val zIndices = remember { mutableMapOf<SceneIdentity, Float>() }
    sceneMap[sceneIdentity] = scene

    val navigationEventState = rememberNavigationEventState(NavigationEventInfo.None)

    // Set up predictive back gesture handling
    HandlePredictiveBack(
        navigationEventState = navigationEventState,
        scene = overlayScenes.lastOrNull() ?: scene,
        state = state,
    )

    // Create the transition state that manages animations between scenes
    val transitionState = remember { SeekableTransitionState(sceneState) }
    val transition = rememberTransition(transitionState, label = "scene")

    // Track entries from the transition's current state for isPop detection
    // (like NavDisplay's transitionCurrentStateEntries)
    val transitionCurrentStateEntries = remember(transition.currentState) {
        state.backstack.map { it.id }
    }

    // Gesture state
    val gestureTransition = navigationEventState.transitionState
    val progress = when (gestureTransition) {
        is NavigationEventTransitionState.Idle -> 0f
        is NavigationEventTransitionState.InProgress -> gestureTransition.latestEvent.progress
    }
    val inPredictiveBack = gestureTransition is NavigationEventTransitionState.InProgress

    // Calculate previous scene for predictive back (like NavDisplay's previousScene)
    val activeScene = overlayScenes.lastOrNull() ?: scene
    val previousSceneState = if (inPredictiveBack && activeScene !is NavigationScene.Overlay) {
        calculatePreviousScene(activeScene, sceneStrategy)?.let { previousScene ->
            SceneState(previousScene, state.container.key).also {
                sceneMap[it.identity] = previousScene
            }
        }
    } else null

    // Determine if this is a pop (back) navigation (like NavDisplay's isPop)
    val isPop = isPop(
        transitionCurrentStateEntries,
        state.backstack.map { it.id },
    )

    // Z-index management (like NavDisplay) — keyed by identity, not the
    // SceneState wrapper, so the z-index survives across SceneState
    // instances that share an identity (i.e. same scene type+key, new
    // entries inside).
    val initialIdentity = transition.currentState.identity
    val targetIdentity = transition.targetState.identity
    val initialZIndex = zIndices.getOrPut(initialIdentity) { 0f }
    val targetZIndex = when {
        initialIdentity == targetIdentity -> initialZIndex
        isPop || inPredictiveBack -> initialZIndex - 1f
        else -> initialZIndex + 1f
    }
    zIndices[targetIdentity] = targetZIndex

    // Transition handling (like NavDisplay). We drive on `sceneState`,
    // not on `sceneIdentity`, so a same-identity scene whose entries
    // changed still propagates to the transition's currentState (and
    // hence into the AnimatedContent content lambda, so the scene's
    // content() runs with the new entries). AnimatedContent will not
    // run an enter/exit animation in that case because the contentKey
    // (the identity) is unchanged.
    if (inPredictiveBack && previousSceneState != null) {
        // During predictive back, seek to the previous scene based on gesture progress
        if (transition.currentState != previousSceneState) {
            LaunchedEffect(previousSceneState, progress) {
                transitionState.seekTo(progress, previousSceneState)
            }
        }
    } else {
        LaunchedEffect(sceneState) {
            if (transitionState.currentState != sceneState) {
                // Animate to the new scene
                transitionState.animateTo(sceneState)
            } else {
                // Predictive back has either been completed or cancelled
                // so now we need to seekTo+snapTo the final state
                // (like NavDisplay's settle animation)

                // convert from nanoseconds to milliseconds
                val totalDuration = transition.totalDurationNanos / 1000000
                // Which way we have to seek depends on whether the
                // predictive back was completed or cancelled
                val predictiveBackCompleted = transition.targetState == sceneState
                val (finalFraction, remainingDuration) = if (predictiveBackCompleted) {
                    // If it completed, animate to the state we were
                    // already seeking to with the remaining duration
                    1f to ((1f - transitionState.fraction) * totalDuration).toInt()
                } else {
                    // If it got cancelled, animate back to the
                    // initial state, reversing what we seeked to
                    0f to (transitionState.fraction * totalDuration).toInt()
                }
                animate(
                    transitionState.fraction,
                    finalFraction,
                    animationSpec = tween(remainingDuration),
                ) { value, _ ->
                    this@LaunchedEffect.launch {
                        if (value != finalFraction) {
                            // Seek the transition towards the finalFraction
                            transitionState.seekTo(value)
                        }
                        if (value == finalFraction) {
                            // Once the animation finishes, we need to snap to the right state.
                            transitionState.snapTo(sceneState)
                        }
                    }
                }
            }
        }
    }

    // Calculate which destinations should be rendered in each scene.
    // Mirrors Nav3's sceneToExcludedEntryMap with the same pop-swap
    // branch: on pops, the entries of the target scene must be
    // rendered by the *target* and excluded from every other (higher-z)
    // scene, otherwise a shared entry that's "coming back into view"
    // beneath the popping scene won't have a layout slot to bridge to.
    val sceneToRenderableDestinationMap = remember(
        sceneMap.entries.toList(),
        transition.targetState.identity,
        zIndices.toString(),
    ) {
        buildMap {
            val scenesByZDescending = sceneMap.entries
                .sortedByDescending { zIndices[it.key] ?: 0f }
                .toList()

            // If the target isn't the highest-z scene, we're in a pop:
            // a higher-z scene is animating out on top of the target.
            val highestIdentity = scenesByZDescending.firstOrNull()?.key
            val targetIdentity = transition.targetState.identity
            val isPopTransition = highestIdentity != null && highestIdentity != targetIdentity
            val targetEntryIds = transition.targetState.scene.entries
                .map { it.instance.id }
                .toSet()

            // Tracks entries that a higher-z scene has CLAIMED for
            // rendering. We accumulate only the *renderable* set, not
            // the scene's full entries list — otherwise during a pop
            // the popping scene's entries (which it deliberately
            // doesn't render so the target can render them underneath)
            // would still end up in coveredDestinationIds and end up
            // excluded from the target as well, leaving the target
            // scene blank.
            val coveredDestinationIds = mutableSetOf<String>()
            scenesByZDescending.forEach { (identity, scene) ->
                val sceneEntryIds = scene.entries.map { it.instance.id }
                val renderable = if (isPopTransition && identity != targetIdentity) {
                    // Higher-z (popping) scene during a pop: render
                    // only what's NOT in the target scene and NOT
                    // already covered by an even-higher scene.
                    sceneEntryIds
                        .filterNot(coveredDestinationIds::contains)
                        .filterNot(targetEntryIds::contains)
                        .toSet()
                } else {
                    // Push, or this is the target scene during a pop:
                    // render everything not already claimed by a
                    // higher-z scene's renderable set.
                    sceneEntryIds
                        .filterNot(coveredDestinationIds::contains)
                        .toSet()
                }
                put(identity, renderable)
                coveredDestinationIds.addAll(renderable)
            }
        }
    }

    // Select the appropriate transition spec based on navigation type
    val contentTransform: AnimatedContentTransitionScope<out SceneTransitionData>.() -> ContentTransform = {
        val isDifferentContainer = initialState.containerKey != targetState.containerKey
        val useContainerTransition = when {
            isDifferentContainer -> true
            !animations.emptyUsesContainerTransition -> false
            initialState.visible.isEmpty() && targetState.visible.isNotEmpty() -> true
            initialState.visible.isNotEmpty() && targetState.visible.isEmpty() -> true
            else -> false
        }
        when {
            useContainerTransition -> animations.containerTransitionSpec(this)
            inPredictiveBack -> animations.predictivePopTransitionSpec(this)
            isPop -> animations.popTransitionSpec(this)
            else -> animations.transitionSpec(this)
        }
    }

    // Render the navigation content
    CompositionLocalProvider(
        LocalNavigationContainer provides state,
        LocalNavigationContext provides state.context,
    ) {
        ProvideRemovalTrackingInfo {
            SharedTransitionLayout {
                transition.AnimatedContent(
                    // contentKey collapses SceneState -> SceneIdentity, so
                    // a new SceneState with the same identity (e.g. a
                    // TwoPaneScene where the right-pane entry changed)
                    // reuses the same AnimatedContent slot and does not
                    // run an enter/exit transition. Within that slot,
                    // Compose still recomposes with the new SceneState's
                    // scene.content(), so updated entries render.
                    contentKey = { it.identity },
                    contentAlignment = contentAlignment,
                    modifier = modifier,
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = contentTransform(this).targetContentEnter,
                            initialContentExit = contentTransform(this).initialContentExit,
                            // z-index increases during navigate and decreases during pop
                            targetContentZIndex = zIndices[transition.targetState.identity] ?: 0f,
                            sizeTransform = sizeTransform,
                        )
                    }
                ) { targetState ->
                    val targetScene = targetState.scene
                    val targetIdentityForContent = targetState.identity
                    // Provide necessary composition locals for the scene content
                    CompositionLocalProvider(
                        LocalNavigationAnimatedVisibilityScope provides this@AnimatedContent,
                        LocalNavigationAnimatedVisibilityScopeOrNull provides this@AnimatedContent,
                        LocalNavigationSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalNavigationSharedTransitionScopeOrNull provides this@SharedTransitionLayout,
                        LocalDestinationsToRenderInCurrentScene provides (
                            sceneToRenderableDestinationMap[targetIdentityForContent]
                                ?: targetScene.entries.map { it.instance.id }.toSet()
                        )
                    ) {
                        targetScene.content()
                    }
                }
            }

            // Clean up scene book-keeping once the transition is finished (like NavDisplay)
            LaunchedEffect(transition) {
                snapshotFlow { transition.isRunning }
                    .filter { !it }
                    .collect {
                        val currentTargetIdentity = transition.targetState.identity
                        // Creating a copy to avoid ConcurrentModificationException
                        sceneMap.keys.toList().forEach { key ->
                            if (key != currentTargetIdentity) {
                                sceneMap.remove(key)
                            }
                        }
                        // Creating a copy to avoid ConcurrentModificationException
                        zIndices.keys.toList().forEach { key ->
                            if (key != currentTargetIdentity) {
                                zIndices.remove(key)
                            }
                        }
                    }
            }

            // Update settled state based on transition progress
            LaunchedEffect(transition.currentState, transition.targetState) {
                val settled = transition.currentState == transition.targetState
                state.isSettled = settled
            }

            // Show all overlay scenes above the AnimatedContent (like NavDisplay)
            RenderOverlayScenes(overlayScenes)
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
 * @param state The navigation container state
 * @param sceneStrategy The strategy for determining scene organization
 * @return Pair of the main scene and list of overlay scenes
 */
@Composable
private fun calculateScenes(
    state: NavigationContainerState,
    sceneStrategy: NavigationSceneStrategy,
): Pair<NavigationScene, List<NavigationScene.Overlay>> {
    val destinations = state.destinations
    // Start with calculating the first scene from all destinations
    val allScenes = mutableListOf(sceneStrategy.calculateSceneWithSinglePaneFallback(destinations))
    var currentScene = allScenes.last()

    // Process overlay scenes recursively
    // Each overlay scene may have destinations that should be overlaid on it
    while (currentScene is NavigationScene.Overlay && currentScene.overlaidEntries.isNotEmpty()) {
        allScenes += sceneStrategy.calculateSceneWithSinglePaneFallback(currentScene.overlaidEntries)
        currentScene = allScenes.last()
    }

    // The last scene is the main scene, all others are overlays
    val overlayScenes = allScenes.dropLast(1).filterIsInstance<NavigationScene.Overlay>()
    val scene = allScenes.last()

    SceneRecompositionDebugger(
        scene = scene,
        overlayScenes = overlayScenes,
        destinations = destinations,
    )
    return scene to overlayScenes
}

/**
 * Calculates the previous scene for predictive back gesture.
 *
 * This determines what scene to show "underneath" during a back gesture by running
 * the scene strategy on the current scene's previous entries. Overlay scenes are
 * skipped to find the underlying content scene.
 *
 * @param scene The current non-overlay scene
 * @param sceneStrategy The strategy for determining scene organization
 * @return The previous scene, or null if there are no previous entries
 */
@Composable
private fun calculatePreviousScene(
    scene: NavigationScene,
    sceneStrategy: NavigationSceneStrategy,
): NavigationScene? {
    if (scene.previousEntries.isEmpty()) return null
    var previousScene = sceneStrategy.calculateSceneWithSinglePaneFallback(scene.previousEntries)
    while (previousScene is NavigationScene.Overlay && previousScene.previousEntries.isNotEmpty()) {
        previousScene = sceneStrategy.calculateSceneWithSinglePaneFallback(previousScene.previousEntries)
    }
    return previousScene
}

@Composable
internal fun SceneRecompositionDebugger(
    scene: NavigationScene,
    overlayScenes: List<NavigationScene>,
    destinations: List<NavigationDestination<NavigationKey>>
) {
    val sceneHashes = remember {
        mutableStateOf(
            SceneHash(
                scene = scene,
                overlayScenes = overlayScenes,
                destinationIds = destinations.map { it.id }.toSet()
            )
        )
    }
    val recompositionCount = remember { mutableStateOf(0) }
    SideEffect {
        val updatedIds = destinations.map { it.id }.toSet()
        val isSameDestinations = sceneHashes.value.destinationIds == updatedIds
        val isSameScenes = sceneHashes.value.scene == scene && sceneHashes.value.overlayScenes == overlayScenes
        if (isSameDestinations && !isSameScenes) {
            recompositionCount.value++
        } else {
            recompositionCount.value = 0
        }
        if (recompositionCount.value > 10) {
            EnroLog.error("Scenes have changed but destinations have not, causing a recomposition. This may be a bug, caused by a SceneStrategy.calculateScene returning a different scene instance for the same destinations.")
            recompositionCount.value = 0
        }
        sceneHashes.value = SceneHash(
            scene = scene,
            overlayScenes = overlayScenes,
            destinationIds = updatedIds,
        )
    }
}

private data class SceneHash(
    val scene: NavigationScene,
    val overlayScenes: List<NavigationScene>,
    val destinationIds: Set<String>
)

/**
 * Sets up handling for predictive back gestures.
 * Monitors back gesture events and updates the state accordingly.
 *
 * @param scene The current scene (including overlays)
 * @param state The navigation container state to update
 */
@Composable
private fun HandlePredictiveBack(
    navigationEventState: NavigationEventState<out NavigationEventInfo>,
    scene: NavigationScene,
    state: NavigationContainerState,
) {
    val backstack = state.backstack
    val isEnabled = remember(scene.previousEntries, backstack) {
        if (scene.previousEntries.isNotEmpty()) return@remember true
        state.emptyBehavior.isBackHandlerEnabled(backstack)
    }

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = isEnabled,
        onBackCancelled = {
            // Process the canceled back gesture
        },
        onBackCompleted = {
            val previousIds = scene.previousEntries
                .map { it.instance.id }
                .toSet()

            val toCloseDestinations = state.context.children.filter {
                !previousIds.contains(it.id)
            }
            toCloseDestinations.forEach {
                it.getNavigationHandle<NavigationKey>().requestClose()
            }
        }
    )

//    LaunchedEffect(navigationEventState.transitionState) {
//        when (val transitionState = navigationEventState.transitionState) {
//            is NavigationEventTransitionState.InProgress -> {
//                val isProgressConsumed = state.emptyBehavior
//                    .onPredictiveBackProgress(
//                        backstack = scene.previousEntries.map { it.instance }.asBackstack(),
//                        progress = transitionState.latestEvent.progress,
//                    )
//            }
//
//            is NavigationEventTransitionState.Idle -> {
//            }
//        }
//    }
}

public interface SceneTransitionData {
    public val containerKey: NavigationContainer.Key
    public val visible: List<NavigationKey.Instance<NavigationKey>>
    public val previouslyVisible: List<NavigationKey.Instance<NavigationKey>>
}

/**
 * Identifies a scene's slot in AnimatedContent. Combines the scene type
 * with its strategy-controlled key and the owning container. Two scenes
 * with the same SceneIdentity reuse the same AnimatedContent slot — no
 * enter/exit animation fires when transitioning between them, even if
 * the underlying NavigationScene instance (and its entries) have
 * changed. This mirrors Nav3's `AnimatedSceneKey` and gives scene
 * strategies control over re-animation through `scene.key`.
 */
private data class SceneIdentity(
    val sceneType: KClass<*>,
    val key: Any,
    val containerKey: NavigationContainer.Key,
)

/**
 * The transition state flowing through the AnimatedContent for the
 * NavigationDisplay. Carries the live scene reference plus the
 * SceneTransitionData fields the transition spec inspects. Equality is
 * based on the scene reference + container key, so a `remember`-stable
 * scene (returned by a typical scene strategy) won't churn the
 * transition state across recompositions.
 */
private class SceneState(
    val scene: NavigationScene,
    override val containerKey: NavigationContainer.Key,
) : SceneTransitionData {
    override val visible: List<NavigationKey.Instance<NavigationKey>>
        get() = scene.entries.map { it.instance }
    override val previouslyVisible: List<NavigationKey.Instance<NavigationKey>>
        get() = scene.previousEntries.map { it.instance }

    val identity: SceneIdentity = SceneIdentity(
        sceneType = scene::class,
        key = scene.key,
        containerKey = containerKey,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SceneState) return false
        return scene == other.scene && containerKey == other.containerKey
    }

    override fun hashCode(): Int = 31 * scene.hashCode() + containerKey.hashCode()
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
    // Track overlay scenes across recompositions. A scene that has just
    // left `overlayScenes` (e.g. its destination was popped from the
    // backstack) still appears in `rendered` until its exit transition
    // settles — so the renderer can keep calling its `content()` and the
    // destination's lifecycle decorator can run the proper teardown
    // sequence after the animation, not in the middle of it.
    val rendered = remember { mutableStateMapOf<Any, NavigationScene.Overlay>() }
    // Stacking order is insertion order: later-added scenes render on
    // top. We retain entries here even after they've left the active
    // overlay list so the per-scene `AnimatedVisibility` can play its
    // exit transition; cleanup happens via `onFullyHidden` below.
    val keysInOrder = remember { mutableStateListOf<Any>() }

    overlayScenes.forEach { scene ->
        if (scene.key !in rendered) keysInOrder.add(scene.key)
        // Always refresh the stored scene — its `previousEntries` /
        // `entries` may have changed even when the key stayed the same.
        rendered[scene.key] = scene
    }
    val activeKeys = overlayScenes.map { it.key }.toSet()

    // Iterate over a snapshot of the ordered list so removals inside
    // `onFullyHidden` don't perturb the current pass.
    keysInOrder.toList().forEach { key ->
        val scene = rendered[key] ?: return@forEach
        key(scene.key) {
            OverlaySceneRenderer(
                scene = scene,
                visible = key in activeKeys,
                onFullyHidden = {
                    rendered.remove(key)
                    keysInOrder.remove(key)
                },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun OverlaySceneRenderer(
    scene: NavigationScene.Overlay,
    visible: Boolean,
    onFullyHidden: () -> Unit,
) {
    val visibleState = remember { mutableStateOf(false) }
    val transitions = scene.overlayTransitions()
    val transition = updateTransition(targetState = visibleState.value, label = "OverlayVisibility")

    LaunchedEffect(visible) {
        visibleState.value = visible
    }
    // The exit transition has settled once `transition.currentState` is false AND the
    // transition is no longer running. Drop the scene from tracking so
    // its destinations can finish their removal lifecycle.
    LaunchedEffect(transition.currentState, transition.isRunning) {
        if (transition.currentState) return@LaunchedEffect
        if (transition.isRunning) return@LaunchedEffect
        onFullyHidden()
    }

    val allDestinations = scene.entries.map { it.instance.id }.toSet()
    SharedTransitionLayout {
        transition.AnimatedVisibility(
            visible = { it },
            enter = transitions?.enter ?: EnterTransition.None,
            exit = transitions?.exit ?: ExitTransition.None,
        ) {
            CompositionLocalProvider(
                LocalNavigationAnimatedVisibilityScope provides this@AnimatedVisibility,
                LocalNavigationAnimatedVisibilityScopeOrNull provides this@AnimatedVisibility,
                LocalNavigationSharedTransitionScope provides this@SharedTransitionLayout,
                LocalNavigationSharedTransitionScopeOrNull provides this@SharedTransitionLayout,
                LocalDestinationsToRenderInCurrentScene provides allDestinations,
            ) {
                scene.content()
            }
        }
    }
}

/**
 * Reads the optional [OverlayTransitions] off the scene's top entry,
 * if it opted in via `directOverlay(enter, exit)` /
 * `directOverlayWithFade()`. Returns null when the scene wants the
 * legacy snap-in / snap-out treatment.
 */
private fun NavigationScene.Overlay.overlayTransitions(): OverlayTransitions? {
    val entry = entries.lastOrNull() ?: return null
    return entry.metadata[DirectOverlaySceneStrategy.OverlayTransitionsKey]
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
    // entire stack replaced
    if (oldBackStack.first() != newBackStack.first()) return false
    // navigated
    if (newBackStack.size > oldBackStack.size) return false

    val divergingIndex = newBackStack.indices.firstOrNull { index ->
        newBackStack[index] != oldBackStack[index]
    }
    // if newBackStack never diverged from oldBackStack, then it is a clean subset of the oldStack
    // and is a pop
    return divergingIndex == null && newBackStack.size != oldBackStack.size
}
