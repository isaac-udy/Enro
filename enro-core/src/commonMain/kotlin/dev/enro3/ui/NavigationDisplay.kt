package dev.enro3.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.enro.animation.rememberTransitionCompat
import dev.enro3.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
public fun NavigationDisplay(
    container: NavigationContainer,
    modifier: Modifier = Modifier,
    sceneStrategy: NavigationSceneStrategy = SinglePaneScene(),
    contentAlignment: Alignment = Alignment.TopStart,
    sizeTransform: SizeTransform? = null,
    transitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        ContentTransform(
            fadeIn(spring(stiffness = Spring.StiffnessMedium)) + slideInHorizontally { it / 3 },
            slideOutHorizontally { -it / 4 },
        )
    },
    popTransitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        ContentTransform(
            slideInHorizontally { -it / 4 },
            fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutHorizontally { it / 3 },
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

    val destinations = backstack
        .map { instance ->
            @Suppress("UNCHECKED_CAST")
            val binding = controller.bindings.bindingFor(instance) as NavigationBinding<NavigationKey>
            binding.provider.create(instance as NavigationKey.Instance<NavigationKey>)
        }
        .map {
            decorateNavigationDestination(
                destination = it,
                destinationDecorators = listOf(
                    navigationContextDecorator<NavigationKey>(backstack, isSettled)
                )
            )
        }

    val scenes = remember { mutableStateMapOf<Any, NavigationScene>() }
    val mostRecentSceneKeys = remember { mutableStateListOf<Any>() }

    val scene = sceneStrategy.calculateScene(destinations) { count ->
        container.execute(NavigationOperation.closeByCount(count))
    }

    if (scene != null) {
        scenes[scene.key] = scene
    }

    // Predictive Back Handling
    var progress by remember { mutableFloatStateOf(0f) }
    var inPredictiveBack by remember { mutableStateOf(false) }

    NavigationBackHandler(scene?.previousEntries?.isNotEmpty() == true) { navEvent ->
        progress = 0f
        try {
            navEvent.collect { value ->
                inPredictiveBack = true
                progress = value.progress
            }
            inPredictiveBack = false
            val count = destinations.size - (scene?.previousEntries?.size ?: 0)
            container.execute(NavigationOperation.closeByCount(count))
        } finally {
            inPredictiveBack = false
        }
    }

    // Scene Handling
    val sceneKey = scene?.key

    val transitionState = remember {
        SeekableTransitionState(sceneKey ?: Unit)
    }

    val transition = rememberTransitionCompat(transitionState, label = sceneKey.toString())

    LaunchedEffect(transition.targetState) {
        if (mostRecentSceneKeys.lastOrNull() != transition.targetState) {
            mostRecentSceneKeys.remove(transition.targetState)
            mostRecentSceneKeys.add(transition.targetState)
        }
    }

    // Transition Handling
    val transitionCurrentStateEntries = remember(transition.currentState) { destinations.toList() }

    // Consider this a pop if the current entries match the previous entries
    val isPop = isPop(
        transitionCurrentStateEntries.map { it.instance.id },
        destinations.map { it.instance.id }
    )

    val zIndices = remember { mutableMapOf<Any, Float>() }
    val initialKey = transition.currentState
    val targetKey = transition.targetState
    val initialZIndex = zIndices.getOrPut(initialKey) { 0f }
    val targetZIndex = when {
        initialKey == targetKey -> initialZIndex
        isPop || inPredictiveBack -> initialZIndex - 1f
        else -> initialZIndex + 1f
    }
    zIndices[targetKey] = targetZIndex

    if (inPredictiveBack && scene != null) {
        val peekScene = sceneStrategy.calculateScene(scene.previousEntries) { count ->
            container.execute(NavigationOperation.closeByCount(count))
        }
        if (peekScene != null) {
            scenes[peekScene.key] = peekScene
            if (transitionState.currentState != peekScene.key) {
                LaunchedEffect(progress) { transitionState.seekTo(progress, peekScene.key) }
            }
        }
    } else {
        val currentSceneKey = sceneKey ?: Unit
        LaunchedEffect(currentSceneKey) {
            if (transitionState.currentState != currentSceneKey) {
                transitionState.animateTo(currentSceneKey)
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
                            transitionState.snapTo(currentSceneKey)
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
        LocalNavigationContainer provides container
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
            scenes[targetSceneKey]?.content?.invoke()
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