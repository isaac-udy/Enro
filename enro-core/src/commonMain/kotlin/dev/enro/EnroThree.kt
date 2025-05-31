package dev.enro

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.enro.animation.rememberTransitionCompat
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


/**
 * Is the root level container the window manager? Do I still want to be doing per-platform window stuff?
 *
 * Probably not TBH. It's a bit more flexible to allow a synthetic destination or something to actually do the
 * platform specific navigation. Desktop windowing can be handled reasonably easily through scenes (because
 * the windows end up just being composable, but for this to work on Android/iOS, we might need to consider a
 * slightly different method.
 *
 * Perhaps it does make sense to try and have some kind of root level scene? Will be tricky though.
 *
 * Synthetics could be left in the backstack as an invisible thing to be a bridge between certain platform behaviours?
 * Might not actually make sense. But also means we could use a launched effect for synthetics.
 */

/**
 * A NavigationInstruction is a command that is sent to Enro to perform some action
 *
 * It might be a nice idea to actually separate these into different instructions, there's actually no reason that these
 * need to be off the same sealed hierarchy.
 *
 * Would it be a good idea to simplify everything down to a container instruction? There's difficulties there with
 * requestClose vs. close vs. closeWithResult, but could generally help?
 */


/**
 * Do all NavigationDestinations need to be Composable, or should they just mostly be? I think it might be possible
 * to get away with Composable only destinations, and then rely on winder rendering to handle non-Composable type destinations,
 * but how exactly is non-Composable/window rendering actually going to work? It can't really flow through the regular channels,
 * but that's probably not a bad thing. Windows can be windows on desktop very easily. Windows on the web are tricky no matter what,
 * but it's Android and iOS that want to do picture in picture and scenes and shit like that that will make this more difficult.
 *
 * Is Window the right term or would "Root" be more accurate? Root in a tab on web, Root in an activity, Root in a window scene, etc
 *
 * There's also the back behaviour for desktop that needs consideration; web is reasonably straight forward, iOS and Android
 * are also reasonably straight forward, but are we maknig people bind the escape key presses on every window? Are we allowing
 * people to drop random Window {} composables into composition anyhwere? I feel like we don't want to do that, but I'm not entirely
 * sure...
 */
// NavigationDestination -> ComposableDestination, SyntheticDestination, FragmentDestination, DialogDestination, ...
// BindDestination() / RegisterDestination()
// BindNavigation() / RegisterNavigation()
// BindPath() / RegisterPath()

public val LocalNavigationContainer: ProvidableCompositionLocal<NavigationContainer> = staticCompositionLocalOf {
    error("No LocalNavigationContainer")
}

public val LocalNavigationHandle: ProvidableCompositionLocal<NavigationHandle<out NavigationKey>> =
    staticCompositionLocalOf {
        error("No LocalNavigationHandle")
    }

public interface NavigationScene {
    public val key: Any
    public val entries: List<NavigationDestination<out NavigationKey>>
    public val previousEntries: List<NavigationDestination<out NavigationKey>>
    public val content: @Composable () -> Unit
}

public interface NavigationSceneStrategy {
    @Composable
    public fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (count: Int) -> Unit
    ): NavigationScene?
}

public class SinglePaneScene : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (Int) -> Unit
    ): NavigationScene {
        return object : NavigationScene {

            override val entries: List<NavigationDestination<out NavigationKey>> = listOf(entries.last())
            override val key: Any = SinglePaneScene::class to entries.map { it.instance.id }

            override val previousEntries: List<NavigationDestination<out NavigationKey>> = entries.dropLast(1)
            override val content: @Composable (() -> Unit) = {
                this.entries.single().content()
            }
        }
    }
}


public class DoublePaneScene : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (Int) -> Unit
    ): NavigationScene {
        return object : NavigationScene {

            override val entries: List<NavigationDestination<out NavigationKey>> = entries.takeLast(2)
            override val key: Any = DoublePaneScene::class to entries.map { it.instance.id }

            override val previousEntries: List<NavigationDestination<out NavigationKey>> = entries.dropLast(1)
            override val content: @Composable (() -> Unit) = {
                Column {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        entries[0].content()
                    }
                    if (entries.size > 1) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            entries[1].content()
                        }
                    }
                }
            }
        }
    }
}

public var destinations: MutableMap<KClass<out NavigationKey>, NavigationDestinationProvider<out NavigationKey>> =
    mutableMapOf()

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
    val backstack = container.backstack.collectAsState().value
    require(backstack.isNotEmpty()) { "NavigationDisplay backstack cannot be empty" }

    var isSettled by remember { mutableStateOf(true) }

    val destinations = backstack
        .map { instance ->
            val destination = destinations[instance.key::class] as NavigationDestinationProvider<NavigationKey>
            destination.create(instance as NavigationKey.Instance<NavigationKey>)
        }
        .map {
            decorateNavigationDestination(
                destination = it,
                destinationDecorators = listOf(
                    localNavigationContextDecorator<NavigationKey>(backstack, isSettled)
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
