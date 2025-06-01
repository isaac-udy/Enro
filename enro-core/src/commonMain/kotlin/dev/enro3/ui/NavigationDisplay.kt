package dev.enro3.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEachReversed
import dev.enro3.*
import dev.enro3.ui.animation.rememberTransitionCompat
import dev.enro3.ui.scenes.DialogSceneStrategy
import dev.enro3.ui.scenes.DirectOverlaySceneStrategy
import dev.enro3.ui.scenes.SinglePaneScene
import dev.enro3.ui.scenes.calculateSceneWithSinglePaneFallback
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

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
    val controller = remember {
        requireNotNull(EnroController.instance) {
            "EnroController must be initialized before using NavigationDisplay"
        }
    }
    val backstack = container.backstack.collectAsState().value
    require(backstack.isNotEmpty()) { "NavigationDisplay backstack cannot be empty" }

    var isSettled by remember { mutableStateOf(true) }

    val movableContentDecorator = rememberMovableContentDestinationDecorator<NavigationKey>()
    val navigationContextDecorator = remember(backstack, isSettled) {
        navigationContextDecorator<NavigationKey>(backstack, isSettled)
    }
    val destinations = remember(backstack) {
        backstack.map { instance ->
            @Suppress("UNCHECKED_CAST")
            val binding = controller.bindings.bindingFor(instance) as NavigationBinding<NavigationKey>
            binding.provider.create(instance as NavigationKey.Instance<NavigationKey>)
        }
            .map {
                decorateNavigationDestination(
                    destination = it,
                    destinationDecorators = listOf(
                        movableContentDecorator,
                        navigationContextDecorator,
                    )
                )
            }
    }

    // Calculate all scenes, starting with the main scene and then processing overlay scenes
    val onBack: (Int) -> Unit = { count ->
        container.execute(NavigationOperation.closeByCount(count))
    }

    val allScenes = mutableListOf(sceneStrategy.calculateSceneWithSinglePaneFallback(destinations, onBack))
    do {
        val overlayScene = allScenes.last() as? NavigationScene.Overlay
        val overlaidEntries = overlayScene?.overlaidEntries
        if (overlaidEntries != null) {
            require(overlaidEntries.isNotEmpty()) {
                "Overlaid entries from $overlayScene must not be empty"
            }
            allScenes += sceneStrategy.calculateSceneWithSinglePaneFallback(overlaidEntries, onBack)
        }
    } while (overlaidEntries != null)

    val overlayScenes = allScenes.dropLast(1)
    val scene = allScenes.last()

    val scenes = remember { mutableStateMapOf<Pair<KClass<*>, Any>, NavigationScene>() }
    val mostRecentSceneKeys = remember { mutableStateListOf<Pair<KClass<*>, Any>>() }

    val sceneKey = scene::class to scene.key
    scenes[sceneKey] = scene

    // Predictive Back Handling
    var progress by remember { mutableFloatStateOf(0f) }
    var inPredictiveBack by remember { mutableStateOf(false) }

    NavigationBackHandler(scene.previousEntries.isNotEmpty()) { navEvent ->
        progress = 0f
        try {
            navEvent.collect { value ->
                inPredictiveBack = true
                progress = value.progress
            }
            inPredictiveBack = false
            onBack(destinations.size - scene.previousEntries.size)
        } finally {
            inPredictiveBack = false
        }
    }

    // Scene Handling
    val transitionState = remember {
        SeekableTransitionState(sceneKey)
    }

    val transition = rememberTransitionCompat(transitionState, label = sceneKey.toString())

    LaunchedEffect(transition.targetState) {
        if (mostRecentSceneKeys.lastOrNull() != transition.targetState) {
            mostRecentSceneKeys.remove(transition.targetState)
            mostRecentSceneKeys.add(transition.targetState)
        }
    }

    // Determine which destinations should be rendered within each scene.
    // Each renderable Scene, in order from the scene that is most recently the target scene to
    // the scene that is least recently the target scene will be assigned each visible
    // entry that hasn't already been assigned to a Scene that is more recent.
    val sceneToRenderableDestinationMap = remember(
        mostRecentSceneKeys.toList(),
        scenes.values.map { scene -> scene.entries.map { it.instance.id } },
        transition.targetState,
    ) {
        buildMap {
            val coveredDestinationIds = mutableSetOf<String>()
            (mostRecentSceneKeys.filter { it != transition.targetState } + listOf(transition.targetState))
                .fastForEachReversed { sceneKey ->
                    val scene = scenes.getValue(sceneKey)
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

    // Transition Handling
    val transitionCurrentStateEntries = remember(transition.currentState) { destinations.toList() }

    // Consider this a pop if the current entries match the previous entries
    val isPop = isPop(
        transitionCurrentStateEntries.map { it.instance.id },
        destinations.map { it.instance.id }
    )

    val zIndices = remember { mutableMapOf<Pair<KClass<*>, Any>, Float>() }
    val initialKey = transition.currentState
    val targetKey = transition.targetState
    val initialZIndex = zIndices.getOrPut(initialKey) { 0f }
    val targetZIndex = when {
        initialKey == targetKey -> initialZIndex
        isPop || inPredictiveBack -> initialZIndex - 1f
        else -> initialZIndex + 1f
    }
    zIndices[targetKey] = targetZIndex

    if (inPredictiveBack) {
        val peekScene = sceneStrategy.calculateSceneWithSinglePaneFallback(scene.previousEntries, onBack)
        val peekSceneKey = peekScene::class to peekScene.key
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

    val contentTransform: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        when {
            inPredictiveBack -> predictivePopTransitionSpec(this)
            isPop -> popTransitionSpec(this)
            else -> transitionSpec(this)
        }
    }

    CompositionLocalProvider(
        LocalNavigationContainer provides container,
        LocalDestinationsToRenderInCurrentScene provides sceneToRenderableDestinationMap.getValue(transition.targetState)
    ) {
        transition.AnimatedContent(
            contentAlignment = contentAlignment,
            modifier = modifier,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = contentTransform(this).targetContentEnter,
                    initialContentExit = contentTransform(this).initialContentExit,
                    targetContentZIndex = targetZIndex,
                    sizeTransform = sizeTransform,
                )
            }
        ) { targetSceneKey ->
            val targetScene = scenes.getValue(targetSceneKey)
            CompositionLocalProvider(
                LocalDestinationsToRenderInCurrentScene provides sceneToRenderableDestinationMap.getValue(targetSceneKey)
            ) {
                targetScene.content()
            }
        }
    }

    // Clean-up scene book-keeping once the transition is finished.
    LaunchedEffect(transition) {
        snapshotFlow { transition.isRunning }
            .filter { !it }
            .collect {
                scenes.keys.toList().forEach { key ->
                    if (key != transition.targetState) {
                        scenes.remove(key)
                    }
                }
                mostRecentSceneKeys.toList().forEach { key ->
                    if (key != transition.targetState) {
                        mostRecentSceneKeys.remove(key)
                    }
                }
            }
    }

    LaunchedEffect(transition.currentState, transition.targetState) {
        val settled = transition.currentState == transition.targetState
        isSettled = settled
    }

    // Show all OverlayNavigationScene instances above the AnimatedContent
    overlayScenes.fastForEachReversed { overlayScene ->
        val allDestinations = overlayScene.entries.map { it.instance.id }.toSet()
        CompositionLocalProvider(
            LocalNavigationContainer provides container,
            LocalDestinationsToRenderInCurrentScene provides allDestinations
        ) {
            overlayScene.content()
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
