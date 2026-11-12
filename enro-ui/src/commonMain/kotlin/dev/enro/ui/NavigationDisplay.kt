package dev.enro.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.platform.EnroLog
import dev.enro.requestClose
import dev.enro.ui.animation.NonDecreasingFrameTime
import dev.enro.ui.decorators.LocalNavigationDestinationWrapper
import dev.enro.ui.decorators.SharedElementDestinationWrapper
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.OverlayTransitions
import dev.enro.ui.scenes.OverlayTransitionsKey
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import dev.enro.viewmodel.getNavigationHandle
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@OptIn(ExperimentalSharedTransitionApi::class)
public object NavigationDisplay {
    /**
     * Scene metadata key that overrides [NavigationAnimations.transitionSpec] for a push.
     * NavigationDisplay reads the key from the metadata of the scene being pushed, and does
     * not read the key for a container transition.
     *
     * Equivalent to Navigation 3's `NavDisplay.TransitionKey`.
     */
    public object TransitionKey :
        NavigationScene.MetadataKey<TransitionSpec?>(
            default = null,
            name = "dev.enro.ui.NavigationDisplay.TransitionKey",
        )

    /**
     * Scene metadata key that overrides [NavigationAnimations.popTransitionSpec].
     * NavigationDisplay reads the key from the metadata of the scene being popped, and does
     * not read the key for a container transition.
     */
    public object PopTransitionKey :
        NavigationScene.MetadataKey<TransitionSpec?>(
            default = null,
            name = "dev.enro.ui.NavigationDisplay.PopTransitionKey",
        )

    /**
     * Scene metadata key that overrides [NavigationAnimations.predictivePopTransitionSpec].
     * The spec receives the [androidx.navigationevent.NavigationEvent.SwipeEdge] of the
     * back gesture.
     */
    public object PredictivePopTransitionKey :
        NavigationScene.MetadataKey<PredictivePopTransitionSpec?>(
            default = null,
            name = "dev.enro.ui.NavigationDisplay.PredictivePopTransitionKey",
        )
}

/**
 * The value type of [NavigationDisplay.TransitionKey] and [NavigationDisplay.PopTransitionKey].
 */
public typealias TransitionSpec =
        AnimatedContentTransitionScope<out SceneTransitionData>.() -> ContentTransform

/**
 * The value type of [NavigationDisplay.PredictivePopTransitionKey]. `swipeEdge` is the
 * `NavigationEvent.SwipeEdge` of the back gesture.
 */
public typealias PredictivePopTransitionSpec =
        AnimatedContentTransitionScope<out SceneTransitionData>.(swipeEdge: Int) -> ContentTransform

/**
 * Renders the backstack of [state] as [NavigationScene]s and animates between the scenes.
 * Overlay scenes are rendered above the other scenes.
 *
 * [sceneStrategy] calculates the scenes. When [sceneStrategy] returns no scene, a
 * [SinglePaneSceneStrategy] calculates the scene.
 *
 * Two scenes can contain the same entry. While both scenes are composed, during an animation
 * or a predictive back gesture, only one scene renders the entry: the scene with the higher
 * z-index, or the target scene during a pop.
 *
 * @param sizeTransform The size transform applied to each scene transition
 * @param animations The transitions used when the scene metadata declares no override
 */
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
    sceneDecoratorStrategies: List<SceneDecoratorStrategy> = emptyList(),
    sharedTransitionScope: SharedTransitionScope? = null,
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

    // `onBackInternal` is created before `sceneState` exists, so `onBackInternal` reads the
    // current scene through `sceneStateRef`.
    val sceneStateRef = remember { mutableStateOf<NavigationSceneState?>(null) }
    val onBackInternal: () -> Unit = remember(state) {
        {
            val current = sceneStateRef.value?.currentScene
            if (current != null) {
                val previousIds = current.previousEntries.map { it.instance.id }.toSet()
                state.context.children
                    .filter { it.id !in previousIds }
                    .forEach { it.getNavigationHandle<NavigationKey>().requestClose() }
            }
        }
    }

    val sceneState = rememberNavigationSceneState(
        containerState = state,
        sceneStrategy = sceneStrategy,
        onBack = onBackInternal,
        sceneDecoratorStrategies = sceneDecoratorStrategies,
    )
    sceneStateRef.value = sceneState
    val scene = sceneState.currentScene
    val overlayScenes = sceneState.overlayScenes

    SceneRecompositionDebugger(
        scene = scene,
        overlayScenes = overlayScenes,
        destinations = sceneState.entries,
    )

    val sceneFrame = SceneTransitionFrame(scene, state.container.key)
    val sceneIdentity = sceneFrame.identity

    val sceneMap = remember { mutableStateMapOf<SceneIdentity, NavigationScene>() }
    val zIndices = remember { mutableMapOf<SceneIdentity, Float>() }
    sceneMap[sceneIdentity] = scene

    val navigationEventState = rememberNavigationEventState(NavigationSceneInfo(scene))

    HandlePredictiveBack(
        navigationEventState = navigationEventState,
        scene = overlayScenes.lastOrNull() ?: scene,
        state = state,
    )

    val transitionState = remember { SeekableTransitionState(sceneFrame) }
    val transition = rememberTransition(transitionState, label = "scene")
    // Every call that drives transitionState must use this one NonDecreasingFrameTime.
    val transitionFrameTime = remember { NonDecreasingFrameTime() }

    // The backstack ids read when transition.currentState last changed. isPop compares
    // these ids with the ids of the current backstack.
    val transitionCurrentStateEntries = remember(transition.currentState) {
        state.backstack.map { it.id }
    }

    val gestureTransition = navigationEventState.transitionState
    val inPredictiveBack = gestureTransition is NavigationEventTransitionState.InProgress
    val progress = rememberRemappedBackProgress(gestureTransition)

    val activeScene = overlayScenes.lastOrNull() ?: scene
    val previousSceneFrame = if (inPredictiveBack && activeScene !is NavigationScene.Overlay) {
        sceneState.previousScenes
            .firstOrNull { it !is NavigationScene.Overlay }
            ?.let { previousScene ->
                SceneTransitionFrame(previousScene, state.container.key).also {
                    sceneMap[it.identity] = previousScene
                }
            }
    } else null

    val isPop = isPop(
        transitionCurrentStateEntries,
        state.backstack.map { it.id },
    )

    val initialIdentity = transition.currentState.identity
    val targetIdentity = transition.targetState.identity
    val initialZIndex = zIndices.getOrPut(initialIdentity) { 0f }
    val targetZIndex = when {
        initialIdentity == targetIdentity -> initialZIndex
        isPop || inPredictiveBack -> initialZIndex - 1f
        else -> initialZIndex + 1f
    }
    zIndices[targetIdentity] = targetZIndex

    // transitionState is driven with `sceneFrame`, not `sceneIdentity`, so that a scene with
    // an unchanged identity and changed entries is passed to the AnimatedContent content lambda.
    if (inPredictiveBack && previousSceneFrame != null) {
        if (transition.currentState != previousSceneFrame) {
            LaunchedEffect(previousSceneFrame, progress) {
                transitionFrameTime.runPreventingFrameTimeRegression {
                    transitionState.seekTo(progress, previousSceneFrame)
                }
            }
        }
    } else {
        LaunchedEffect(sceneFrame) {
            if (transitionState.currentState != sceneFrame) {
                transitionFrameTime.runPreventingFrameTimeRegression {
                    transitionState.animateTo(sceneFrame)
                }
            } else {
                // A predictive back gesture was completed or cancelled: animate the remaining
                // fraction, then snap to sceneFrame.
                val totalDuration = transition.totalDurationNanos / 1000000
                val predictiveBackCompleted = transition.targetState == sceneFrame
                val (finalFraction, remainingDuration) = if (predictiveBackCompleted) {
                    1f to ((1f - transitionState.fraction) * totalDuration).toInt()
                } else {
                    0f to (transitionState.fraction * totalDuration).toInt()
                }
                animate(
                    transitionState.fraction,
                    finalFraction,
                    animationSpec = tween(remainingDuration),
                ) { value, _ ->
                    this@LaunchedEffect.launch {
                        transitionFrameTime.runPreventingFrameTimeRegression {
                            if (value != finalFraction) {
                                transitionState.seekTo(value)
                            }
                            if (value == finalFraction) {
                                transitionState.snapTo(sceneFrame)
                            }
                        }
                    }
                }
            }
        }
    }

    // Maps each scene to the entry ids that the scene must not render. The scene with the
    // higher z-index renders a shared entry, except during a pop: the target scene renders
    // the shared entry, so that shared elements in the target scene are composed.
    val sceneToExcludedEntryMap = remember(
        sceneMap.entries.toList(),
        transition.targetState.identity,
        zIndices.toString(),
    ) {
        buildMap {
            val scenesByZDescending = sceneMap.entries
                .sortedByDescending { zIndices[it.key] ?: 0f }
                .toList()

            // The target scene is below another scene only during a pop: the scene above the
            // target scene is animating out.
            val highestIdentity = scenesByZDescending.firstOrNull()?.key
            val targetIdentity = transition.targetState.identity
            val isPopTransition = highestIdentity != null && highestIdentity != targetIdentity
            val targetEntryIds = transition.targetState.scene.entries
                .map { it.instance.id }
                .toSet()

            // The entry ids rendered by the scenes already visited, which have a higher z-index.
            val coveredEntryIds = mutableSetOf<String>()
            scenesByZDescending.forEach { (identity, scene) ->
                val sceneEntryIds = scene.entries.map { it.instance.id }
                if (isPopTransition && identity != targetIdentity) {
                    put(identity, targetEntryIds)
                } else {
                    put(identity, coveredEntryIds.toSet())
                }
                val newlyCovered = sceneEntryIds.filterNot { it in coveredEntryIds }
                if (isPopTransition && identity != targetIdentity) {
                    coveredEntryIds.addAll(newlyCovered.filterNot { it in targetEntryIds })
                } else {
                    coveredEntryIds.addAll(newlyCovered)
                }
            }

            // During a pop, the target scene renders every entry of the target scene,
            // including entries that the popping scene also contains.
            if (isPopTransition) {
                put(targetIdentity, emptySet())
            }
        }
    }

    val swipeEdge = when (val gt = gestureTransition) {
        is NavigationEventTransitionState.Idle -> NavigationEvent.EDGE_NONE
        is NavigationEventTransitionState.InProgress -> gt.latestEvent.swipeEdge
    }

    // The scene with the higher z-index supplies the transition metadata: the target scene
    // on a push, the initial scene on a pop.
    val transitionScene = if (initialZIndex >= targetZIndex) {
        transition.currentState.scene
    } else {
        transition.targetState.scene
    }

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
            inPredictiveBack -> {
                transitionScene.metadata[NavigationDisplay.PredictivePopTransitionKey]
                    ?.invoke(this, swipeEdge)
                    ?: animations.predictivePopTransitionSpec(this, swipeEdge)
            }

            isPop -> {
                transitionScene.metadata[NavigationDisplay.PopTransitionKey]
                    ?.invoke(this)
                    ?: animations.popTransitionSpec(this)
            }

            else -> {
                transitionScene.metadata[NavigationDisplay.TransitionKey]
                    ?.invoke(this)
                    ?: animations.transitionSpec(this)
            }
        }
    }

    CompositionLocalProvider(
        LocalNavigationContainer provides state,
        LocalNavigationContext provides state.context,
        LocalNavigationDestinationWrapper provides SharedElementDestinationWrapper,
    ) {
        WithSharedTransitionScope(sharedTransitionScope) sharedScope@{
            transition.AnimatedContent(
                // Frames with the same identity share one AnimatedContent slot: no enter/exit
                // transition runs, and the slot recomposes with the content of the new frame.
                contentKey = { it.identity },
                contentAlignment = contentAlignment,
                modifier = modifier,
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = contentTransform(this).targetContentEnter,
                        initialContentExit = contentTransform(this).initialContentExit,
                        targetContentZIndex = zIndices[transition.targetState.identity] ?: 0f,
                        sizeTransform = sizeTransform,
                    )
                }
            ) { targetState ->
                val targetScene = targetState.scene
                val targetIdentityForContent = targetState.identity
                CompositionLocalProvider(
                    LocalNavigationAnimatedVisibilityScope provides this@AnimatedContent,
                    LocalNavigationAnimatedVisibilityScopeOrNull provides this@AnimatedContent,
                    LocalNavigationSharedTransitionScope provides this@sharedScope,
                    LocalNavigationSharedTransitionScopeOrNull provides this@sharedScope,
                    LocalEntriesToExcludeFromCurrentScene provides
                            (sceneToExcludedEntryMap[targetIdentityForContent] ?: emptySet())
                ) {
                    targetScene.content()
                }
            }
        }

        LaunchedEffect(transition) {
            snapshotFlow { transition.isRunning }
                .filter { !it }
                .collect {
                    val currentTargetIdentity = transition.targetState.identity
                    // Each loop iterates over a copy of the keys: removing from a map during
                    // iteration throws ConcurrentModificationException.
                    sceneMap.keys.toList().forEach { key ->
                        if (key != currentTargetIdentity) {
                            sceneMap.remove(key)
                        }
                    }
                    zIndices.keys.toList().forEach { key ->
                        if (key != currentTargetIdentity) {
                            zIndices.remove(key)
                        }
                    }
                }
        }

        LaunchedEffect(transition.currentState, transition.targetState) {
            val settled = transition.currentState == transition.targetState
            state.isSettled = settled
        }

        RenderOverlayScenes(overlayScenes)
    }
}

/**
 * Runs [content] in [external] when [external] is not null, so that shared elements can
 * transition between every display inside the caller's `SharedTransitionLayout`.
 * Otherwise wraps [content] in a new `SharedTransitionLayout`.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun WithSharedTransitionScope(
    external: SharedTransitionScope?,
    content: @Composable SharedTransitionScope.() -> Unit,
) {
    if (external != null) {
        with(external) { content() }
    } else {
        SharedTransitionLayout { content() }
    }
}

@Composable
internal fun SceneRecompositionDebugger(
    scene: NavigationScene,
    overlayScenes: List<NavigationScene>,
    destinations: List<NavigationDestination<NavigationKey>>,
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
    val destinationIds: Set<String>,
)

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
}

/**
 * Returns the predictive back progress of [gestureTransition], rescaled so that each gesture
 * starts at 0f. Returns 0f when no gesture is in progress.
 *
 * A UIKit screen-edge pan recognizer emits no event until the pan passes the recognition
 * slop, so the first non-zero progress on iOS is a jump (often ~0.1–0.25), and
 * [androidx.compose.animation.core.SeekableTransitionState.seekTo] applies the jump in one
 * frame. The first non-zero progress of each gesture is stored as an anchor, and progress is
 * rescaled with `(progress - anchor) / (1 - anchor)`. Android progress starts near 0, so the
 * anchor on Android is near 0.
 */
@Composable
private fun rememberRemappedBackProgress(
    gestureTransition: NavigationEventTransitionState,
): Float {
    val inProgress = gestureTransition is NavigationEventTransitionState.InProgress
    val rawProgress = when (gestureTransition) {
        is NavigationEventTransitionState.Idle -> 0f
        is NavigationEventTransitionState.InProgress -> gestureTransition.latestEvent.progress
    }
    // The first non-zero raw progress of the current gesture, or null when no gesture is in
    // progress.
    val anchor = remember { mutableStateOf<Float?>(null) }
    if (!inProgress) {
        if (anchor.value != null) anchor.value = null
    } else if (anchor.value == null && rawProgress > 0f) {
        anchor.value = rawProgress
    }
    return when (val anchorValue = anchor.value) {
        null -> 0f
        // coerceAtLeast prevents a division by zero when the anchor is 1f.
        else -> ((rawProgress - anchorValue) / (1f - anchorValue).coerceAtLeast(1e-4f))
            .coerceIn(0f, 1f)
    }
}

public interface SceneTransitionData {
    public val containerKey: NavigationContainer.Key
    public val visible: List<NavigationKey.Instance<NavigationKey>>
    public val previouslyVisible: List<NavigationKey.Instance<NavigationKey>>
}

/**
 * The AnimatedContent content key for a scene. Two scenes with equal SceneIdentity share one
 * AnimatedContent slot, and no enter/exit animation runs between the two scenes, even when
 * the scene instances or the scene entries differ. A scene strategy controls re-animation
 * through `scene.key`.
 */
private data class SceneIdentity(
    val sceneType: KClass<*>,
    val key: Any,
    val containerKey: NavigationContainer.Key,
)

/**
 * The state type of the NavigationDisplay transition. Equality compares the scene and the
 * container key, so a scene strategy that returns a remembered scene does not change the
 * transition state on recomposition.
 */
private class SceneTransitionFrame(
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
        if (other !is SceneTransitionFrame) return false
        return scene == other.scene && containerKey == other.containerKey
    }

    override fun hashCode(): Int = 31 * scene.hashCode() + containerKey.hashCode()
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RenderOverlayScenes(overlayScenes: List<NavigationScene.Overlay>) {
    // `rendered` contains a scene removed from `overlayScenes` until the scene's onRemove()
    // returns and the scene's exit transition finishes.
    val rendered = remember { mutableStateMapOf<Any, NavigationScene.Overlay>() }
    // Insertion order is stacking order: a scene added later renders above.
    val keysInOrder = remember { mutableStateListOf<Any>() }

    overlayScenes.forEach { scene ->
        if (scene.key !in rendered) keysInOrder.add(scene.key)
        // A scene with an unchanged key can contain changed `entries` or `previousEntries`.
        rendered[scene.key] = scene
    }
    val activeKeys = overlayScenes.map { it.key }.toSet()

    // Iterate over a copy: `onFullyHidden` removes from `keysInOrder`.
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

    // onRemove() runs before the exit transition, because AnimatedVisibility disposes the
    // content when the exit transition finishes. Re-adding the scene cancels the removal.
    LaunchedEffect(visible) {
        if (visible) {
            visibleState.value = true
            return@LaunchedEffect
        }
        scene.onRemove()
        visibleState.value = false
        snapshotFlow { transition.currentState || transition.isRunning }.first { !it }
        onFullyHidden()
    }

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
                LocalEntriesToExcludeFromCurrentScene provides emptySet(),
            ) {
                scene.content()
            }
        }
    }
}

/**
 * Returns the [OverlayTransitions] from the metadata of the scene's last entry: the
 * [OverlayTransitionsKey] value, or a fade built from
 * [DirectOverlaySceneStrategy.OverlayFadeKey]. Returns null when the entry declares neither
 * key; the scene then enters and exits without animation.
 */
private fun NavigationScene.Overlay.overlayTransitions(): OverlayTransitions? {
    val entry = entries.lastOrNull() ?: return null
    entry.metadata[OverlayTransitionsKey]?.let { return it }
    val fadeMillis = entry.metadata[DirectOverlaySceneStrategy.OverlayFadeKey] ?: return null
    return OverlayTransitions(
        enter = fadeIn(animationSpec = tween(fadeMillis)),
        exit = fadeOut(animationSpec = tween(fadeMillis)),
    )
}

/**
 * Returns true when [newBackStack] is not empty and is a strict prefix of [oldBackStack].
 */
private fun <T : Any> isPop(oldBackStack: List<T>, newBackStack: List<T>): Boolean {
    if (oldBackStack.isEmpty() || newBackStack.isEmpty()) return false
    if (oldBackStack.first() != newBackStack.first()) return false
    if (newBackStack.size > oldBackStack.size) return false

    val divergingIndex = newBackStack.indices.firstOrNull { index ->
        newBackStack[index] != oldBackStack[index]
    }
    return divergingIndex == null && newBackStack.size != oldBackStack.size
}
