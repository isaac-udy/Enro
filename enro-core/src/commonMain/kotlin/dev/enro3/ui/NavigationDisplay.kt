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

    val state = remember { NavigationDisplayState() }
    val destinations = rememberDecoratedDestinations(controller, backstack, state.isSettled)

    val (scene, overlayScenes) = calculateScenes(
        destinations = destinations,
        sceneStrategy = sceneStrategy,
        onBack = { count -> container.execute(NavigationOperation.closeByCount(count)) }
    )

    val sceneKey = SceneKey(
        scene::class,
        scene.key
    )
    state.scenes[sceneKey] = scene

    HandlePredictiveBack(
        scene = scene,
        destinations = destinations,
        state = state,
        onBack = { count -> container.execute(NavigationOperation.closeByCount(count)) }
    )

    val transitionState = remember { SeekableTransitionState(sceneKey) }
    val transition = rememberTransitionCompat(transitionState, label = sceneKey.toString())

    val sceneToRenderableDestinationMap = calculateSceneToRenderableDestinationMap(
        state = state,
        transition = transition,
    )

    val isPop = isPop(
        remember(transition.currentState) { destinations.toList() }.map { it.instance.id },
        destinations.map { it.instance.id }
    )

    val zIndices = updateZIndices(
        transition = transition,
        isPop = isPop,
        inPredictiveBack = state.inPredictiveBack
    )

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

    val contentTransform: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        when {
            state.inPredictiveBack -> predictivePopTransitionSpec(this)
            isPop -> popTransitionSpec(this)
            else -> transitionSpec(this)
        }
    }

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

private class NavigationDisplayState {
    var progress by mutableFloatStateOf(0f)
    var inPredictiveBack by mutableStateOf(false)
    var isSettled by mutableStateOf(true)
    val scenes =  mutableStateMapOf<SceneKey, NavigationScene>()
    val mostRecentSceneKeys =  mutableStateListOf<SceneKey>()
}

@Composable
private fun rememberEnroController(): EnroController {
    return remember {
        requireNotNull(EnroController.instance) {
            "EnroController must be initialized before using NavigationDisplay"
        }
    }
}

@Composable
private fun rememberDecoratedDestinations(
    controller: EnroController,
    backstack: List<NavigationKey.Instance<*>>,
    isSettled: Boolean,
): List<NavigationDestination<NavigationKey>> {
    val decorators = listOf(
        rememberMovableContentDecorator(),
        rememberSavedStateDecorator(),
        rememberViewModelStoreDecorator(),
        rememberLifecycleDecorator(backstack, isSettled),
        rememberNavigationContextDecorator(),
    )

    return remember(backstack) {
        backstack
            .map { instance ->
                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<NavigationKey>
                val binding = controller.bindings.bindingFor(instance)
                binding.provider.create(instance)
            }
            .map {
                decorateNavigationDestination(
                    destination = it,
                    decorators = decorators,
                )
            }
    }
}

@Composable
private fun calculateScenes(
    destinations: List<NavigationDestination<NavigationKey>>,
    sceneStrategy: NavigationSceneStrategy,
    onBack: (Int) -> Unit,
): Pair<NavigationScene, List<NavigationScene.Overlay>> {
    val allScenes = mutableListOf(sceneStrategy.calculateSceneWithSinglePaneFallback(destinations, onBack))
    var currentScene = allScenes.last()
    while (currentScene is NavigationScene.Overlay && currentScene.overlaidEntries.isNotEmpty()) {
        allScenes += sceneStrategy.calculateSceneWithSinglePaneFallback(currentScene.overlaidEntries, onBack)
        currentScene = allScenes.last()
    }

    val overlayScenes = allScenes.dropLast(1).filterIsInstance<NavigationScene.Overlay>()
    val scene = allScenes.last()
    return scene to overlayScenes
}

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
            navEvent.collect { value ->
                state.inPredictiveBack = true
                state.progress = value.progress
            }
            state.inPredictiveBack = false
            onBack(destinations.size - scene.previousEntries.size)
        } finally {
            state.inPredictiveBack = false
        }
    }
}

/**
 * A key that uniquely identifies a scene; this is the KClass for the Scene's type, along with the
 * key for the scene's instance.
 */
private data class SceneKey(
    val sceneType: KClass<*>,
    val key: Any,
)

@Composable
private fun calculateSceneToRenderableDestinationMap(
    state: NavigationDisplayState,
    transition: Transition<SceneKey>,
): Map<SceneKey, Set<String>> {
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
            (state.mostRecentSceneKeys.filter { it != transition.targetState } + listOf(transition.targetState))
                .fastForEachReversed { sceneKey ->
                    val scene = state.scenes.getValue(sceneKey)
                    put(
                        sceneKey,
                        scene.entries
                            .map { it.instance.id }
                            .filterNot(coveredDestinationIds::contains)
                            .toSet(),
                    )
                    scene.entries.forEach { coveredDestinationIds.add(it.instance.id) }
                }
        }
    }
}

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
    val targetZIndex = when {
        initialKey == targetKey -> initialZIndex
        isPop || inPredictiveBack -> initialZIndex - 1f
        else -> initialZIndex + 1f
    }
    zIndices[targetKey] = targetZIndex
    return zIndices
}

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
        val peekScene = sceneStrategy.calculateSceneWithSinglePaneFallback(
            scene.previousEntries,
            { /* No-op during predictive back */ }
        )
        val peekSceneKey = SceneKey(
            sceneType = peekScene::class,
            key = peekScene.key,
        )
        scenes[peekSceneKey] = peekScene
        if (transitionState.currentState != peekSceneKey) {
            LaunchedEffect(progress) { transitionState.seekTo(progress, peekSceneKey) }
        }
    } else {
        LaunchedEffect(sceneKey) {
            if (transitionState.currentState != sceneKey) {
                transitionState.animateTo(sceneKey)
            } else {
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

@Composable
private fun CleanupSceneEffect(
    transition: Transition<SceneKey>,
    state: NavigationDisplayState,
) {
    LaunchedEffect(transition) {
        snapshotFlow { transition.isRunning }
            .filter { !it }
            .collect {
                state.scenes.keys.toList().forEach { key ->
                    if (key != transition.targetState) {
                        state.scenes.remove(key)
                    }
                }
                state.mostRecentSceneKeys.toList().forEach { key ->
                    if (key != transition.targetState) {
                        state.mostRecentSceneKeys.remove(key)
                    }
                }
            }
    }
}

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

private fun <T : Any> isPop(oldBackStack: List<T>, newBackStack: List<T>): Boolean {
    if (oldBackStack.isEmpty() || newBackStack.isEmpty()) return false
    if (oldBackStack.first() != newBackStack.first()) return false
    if (newBackStack.size > oldBackStack.size) return false

    val divergingIndex = newBackStack.indices.firstOrNull { index ->
        newBackStack[index] != oldBackStack[index]
    }
    return divergingIndex == null && newBackStack.size != oldBackStack.size
}
