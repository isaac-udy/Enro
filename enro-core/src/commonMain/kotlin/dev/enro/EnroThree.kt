package dev.enro

import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.enro.core.NavigationInstructionExtras
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.uuid.Uuid


/**
 * A NavigationKey represents the contract for a screen; it is the interface that
 * defines the inputs to a screen, and the outputs (if any).
 *
 * NavigationKey.WithExtras is a way of attaching metadata to a NavigationKey before the NavigationKey
 * is turned into a NavigationInstruction
 */
public interface NavigationKey {
    public interface WithResult<T> : NavigationKey

    @ConsistentCopyVisibility
    public data class WithExtras<T : NavigationKey> internal constructor(
        val navigationKey: T,
        val extras: NavigationInstructionExtras,
    )
}

public class NavigationContext<T : NavigationKey>(
    public val destination: NavigationDestination<T>,
    public val parentContainer: NavigationContainer,
    public val childContainers: List<NavigationContainer>,
)

/**
 * NavigationHandle is the bridge to perform navigation actions; a NavigationHandle has a nullable (internal) reference
 * to the navigation context that it is bound to, and the navigation instruction that was used to open
 * that navigation context
 */
public interface NavigationHandle<T : NavigationKey> {
    public val instruction: NavigationInstruction.Open<T>
    public val container: NavigationContainer
    public val key: T get() = instruction.navigationKey
}
public typealias AnyNavigationHandle = NavigationHandle<out NavigationKey>

public typealias NavigationBackstack = List<NavigationInstruction.Open<out NavigationKey>>

/**
 * A NavigationContainer is an identifiable backstack (using navigation container key), which
 * provides the rendering context for a backstack.
 *
 * It's probably the NavigationContainer that needs to be able to host NavigationScenes/NavigationRenderers\
 *
 * Instead of having a CloseParent/AllowEmpty, we should provide a special "Empty" instruction here (maybe even with a
 * placeholder) so that the close behaviour is always consistent (easier for predictive back stuff).
 */
public class NavigationContainer {
    public val key: Any = Unit
    public val backstack: SnapshotStateList<NavigationInstruction.Open<*>> = mutableStateListOf()

//    public val interceptor: NavigationInstructionInterceptor = TODO()
//    public val filter: NavigationInstructionFilter = TODO()

    // Root containers might need slightly different handling?
    public val parent: NavigationContainer? = null
}

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
public sealed interface NavigationInstruction {
    public class Open<T : NavigationKey>(
        public val navigationKey: T
    ) : NavigationInstruction {
        public val id: String = Uuid.random().toString()
    }

    public class Container : NavigationInstruction
    public sealed class Close : NavigationInstruction {
        public class WithResult<T> : Close()
    }

    public class RequestClose : NavigationInstruction
}


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

public class NavigationDestinationProvider<T : NavigationKey>(
    private val metadata: Map<String, Any> = emptyMap(),
    private val content: @Composable () -> Unit,
) {
    public fun create(instruction: NavigationInstruction.Open<T>): NavigationDestination<T> {
        return object : NavigationDestination<T>(
            instruction = instruction,
            metadata = metadata,
            content = content,
        ) {}
    }
}
public open class NavigationDestination<T : NavigationKey>(
    public val instruction: NavigationInstruction.Open<T>,
    public val metadata: Map<String, Any> = emptyMap(),
    public open val content: @Composable () -> Unit,
)

public open class NavigationDestinationWrapper<T : NavigationKey>(
    wrapped: NavigationDestination<T>,
    content: @Composable (content: @Composable () -> Unit) -> Unit
) : NavigationDestination<T>(
    instruction = wrapped.instruction,
    metadata = wrapped.metadata,
    content = { content(wrapped.content) },
)

// We probably want to get rid of push/present and let scenes handle those

public fun <T: NavigationKey> navigationDestination(
    metadata: Map<String, Any> = emptyMap(),
    content: @Composable () -> Unit
): NavigationDestinationProvider<T> {
    return NavigationDestinationProvider(metadata, content)
}

public val LocalNavigationContainer: ProvidableCompositionLocal<NavigationContainer> = staticCompositionLocalOf {
    error("No LocalNavigationContainer")
}

public val LocalNavigationContext: ProvidableCompositionLocal<NavigationContext<out NavigationKey>> = staticCompositionLocalOf {
    error("No LocalNavigationContext")
}

public val LocalNavigationHandle: ProvidableCompositionLocal<NavigationContext<out NavigationKey>> = staticCompositionLocalOf {
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
    public fun calculateScene(entries: List<NavigationDestination<out NavigationKey>>, onBack: (count: Int) -> Unit): NavigationScene?
}

public class SinglePaneScene : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (Int) -> Unit
    ): NavigationScene {
        return object : NavigationScene {

            override val entries: List<NavigationDestination<out NavigationKey>> = listOf(entries.last())
            override val key: Any = SinglePaneScene::class to entries.map { it.instruction.id }

            override val previousEntries: List<NavigationDestination<out NavigationKey>> = entries.dropLast(1)
            override val content: @Composable (() -> Unit) = {
                this.entries.single().content()
            }
        }
    }
}

public var destinations: MutableMap<KClass<out NavigationKey>, NavigationDestinationProvider<out NavigationKey>> = mutableMapOf()

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
    require(container.backstack.isNotEmpty()) { "NavigationDisplay backstack cannot be empty" }
    
    var isSettled by remember { mutableStateOf(true) }
    
    val destinations = container.backstack
        .map { instruction ->
            val destination = destinations[instruction.navigationKey::class] as NavigationDestinationProvider<NavigationKey>
            destination.create(instruction as NavigationInstruction.Open<NavigationKey>)
        }
        .map {
            localNavigationContextWrapper(it)
        }
    
    val scenes = remember { mutableStateMapOf<Any, NavigationScene>() }
    val mostRecentSceneKeys = remember { mutableStateListOf<Any>() }
    
    val scene = sceneStrategy.calculateScene(destinations) { count ->
        repeat(count) { 
            if (container.backstack.isNotEmpty()) {
                container.backstack.removeAt(container.backstack.lastIndex) 
            }
        }
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
            repeat(count) {
                if (container.backstack.isNotEmpty()) {
                    container.backstack.removeAt(container.backstack.lastIndex)
                }
            }
        } finally {
            inPredictiveBack = false
        }
    }

    // Scene Handling
    val sceneKey = scene?.key

    val transitionState = remember {
        SeekableTransitionState(sceneKey ?: Unit)
    }

    val transition = rememberTransition(transitionState, label = sceneKey.toString())

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
        transitionCurrentStateEntries.map { it.instruction.id }, 
        destinations.map { it.instruction.id }
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
            repeat(count) { 
                if (container.backstack.isNotEmpty()) {
                    container.backstack.removeAt(container.backstack.lastIndex) 
                }
            }
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

public fun localNavigationContextWrapper(wrapped: NavigationDestination<*>): NavigationDestinationWrapper<*> =
    NavigationDestinationWrapper(wrapped) { content ->
        val container = LocalNavigationContainer.current
        val context = NavigationContext(
            destination = wrapped,
            parentContainer = container,
            childContainers = emptyList()
        )
        CompositionLocalProvider(
            LocalNavigationContext provides context
        ) {
            content()
        }
    }


@Composable
internal expect fun NavigationBackHandler(
    enabled: Boolean = true,
    onBack: suspend (progress: Flow<NavigationBackEvent>) -> Unit
)

public class NavigationBackEvent(
    /**
     * Absolute X location of the touch point of this event in the coordinate space of the screen
     * that received this navigation event.
     */
    public val touchX: Float,
    /**
     * Absolute Y location of the touch point of this event in the coordinate space of the screen
     * that received this navigation event.
     */
    public val touchY: Float,
    /** Value between 0 and 1 on how far along the back gesture is. */
    public val progress: Float,
    /** Indicates which edge the swipe starts from. */
    public val swipeEdge: @SwipeEdge Int,
    /** Frame time of the navigation event. */
    public val frameTimeMillis: Long = 0,
) {

    /**  */
    @Target(AnnotationTarget.TYPE)
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(EDGE_LEFT, EDGE_RIGHT, EDGE_NONE)
    public annotation class SwipeEdge

    public companion object {
        /** Indicates that the edge swipe starts from the left edge of the screen */
        public const val EDGE_LEFT: Int = 0

        /** Indicates that the edge swipe starts from the right edge of the screen */
        public const val EDGE_RIGHT: Int = 1

        /**
         * Indicates that the back event was not triggered by an edge swipe back gesture. This
         * applies to cases like using the back button in 3-button navigation or pressing a hardware
         * back button.
         */
        public const val EDGE_NONE: Int = 2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NavigationBackEvent

        if (touchX != other.touchX) return false
        if (touchY != other.touchY) return false
        if (progress != other.progress) return false
        if (swipeEdge != other.swipeEdge) return false
        if (frameTimeMillis != other.frameTimeMillis) return false

        return true
    }

    override fun hashCode(): Int {
        var result = touchX.hashCode()
        result = 31 * result + touchY.hashCode()
        result = 31 * result + progress.hashCode()
        result = 31 * result + swipeEdge
        result = 31 * result + frameTimeMillis.hashCode()
        return result
    }

    override fun toString(): String {
        return "NavigationEvent(touchX=$touchX, touchY=$touchY, progress=$progress, swipeEdge=$swipeEdge, frameTimeMillis=$frameTimeMillis)"
    }
}


