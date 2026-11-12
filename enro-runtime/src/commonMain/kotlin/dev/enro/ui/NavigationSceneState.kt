package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import dev.enro.NavigationKey
import dev.enro.ui.scenes.EmptyNavigationScene
import dev.enro.ui.scenes.calculateSceneWithSinglePaneFallback

/**
 * The scenes calculated for a [NavigationContainerState] by [rememberNavigationSceneState].
 * [NavigationDisplay] renders a [NavigationSceneState]. Navigation 3 equivalent: `SceneState<T>`.
 *
 * @property entries the decorated destinations the scenes were calculated from
 * @property overlayScenes the overlay scenes above [currentScene], top-most first
 * @property currentScene the scene below every overlay scene. [currentScene] is an overlay
 *   scene only when that overlay scene's `overlaidEntries` is empty.
 * @property previousScenes the scenes calculated by following `previousEntries` from
 *   [currentScene] until `previousEntries` is empty, nearest scene first. During a
 *   predictive back gesture, [NavigationDisplay] renders the first non-overlay scene in
 *   [previousScenes].
 */
@Immutable
public class NavigationSceneState internal constructor(
    public val entries: List<NavigationDestination<NavigationKey>>,
    public val overlayScenes: List<NavigationScene.Overlay>,
    public val currentScene: NavigationScene,
    public val previousScenes: List<NavigationScene>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NavigationSceneState) return false
        return entries == other.entries &&
            overlayScenes == other.overlayScenes &&
            currentScene == other.currentScene &&
            previousScenes == other.previousScenes
    }

    override fun hashCode(): Int {
        var result = entries.hashCode()
        result = 31 * result + overlayScenes.hashCode()
        result = 31 * result + currentScene.hashCode()
        result = 31 * result + previousScenes.hashCode()
        return result
    }

    override fun toString(): String {
        return "NavigationSceneState(entries=$entries, overlayScenes=$overlayScenes, " +
            "currentScene=$currentScene, previousScenes=$previousScenes)"
    }
}

/**
 * Calculates the [NavigationSceneState] for [containerState]. Navigation 3 equivalent:
 * `rememberSceneState`.
 *
 * A container with no destinations returns a state with no entries and a scene that renders no
 * content, without calling [sceneStrategy]. [SceneStrategyScope.onBack] in each strategy calls
 * [onBack]. [sceneDecoratorStrategies] are applied to [NavigationSceneState.currentScene] and
 * to each of the [NavigationSceneState.previousScenes], and never to an overlay scene.
 */
@Composable
public fun rememberNavigationSceneState(
    containerState: NavigationContainerState,
    sceneStrategy: NavigationSceneStrategy,
    onBack: () -> Unit,
    sceneDecoratorStrategies: List<SceneDecoratorStrategy> = emptyList(),
): NavigationSceneState {
    val currentOnBack by rememberUpdatedState(onBack)
    val scope = remember { SceneStrategyScope(onBack = { currentOnBack() }) }
    val decoratorScope = remember { SceneDecoratorStrategyScope(onBack = { currentOnBack() }) }
    val destinations = containerState.destinations

    // Invariant: `NavigationSceneStrategy.calculateScene` never receives an empty entries list.
    // A container using `EmptyBehavior.allowEmpty()` can have no destinations.
    if (destinations.isEmpty()) {
        return NavigationSceneState(
            entries = emptyList(),
            overlayScenes = emptyList(),
            currentScene = EmptyNavigationScene,
            previousScenes = emptyList(),
        )
    }

    val resolved = resolveSceneChain(scope, destinations, sceneStrategy)
    val overlayScenes = resolved.dropLast(1).filterIsInstance<NavigationScene.Overlay>()
    val currentScene = applyDecorators(decoratorScope, resolved.last(), sceneDecoratorStrategies, containerState)
    val previousScenes = computePreviousScenes(scope, decoratorScope, currentScene, sceneStrategy, sceneDecoratorStrategies, containerState)
    return NavigationSceneState(
        entries = destinations,
        overlayScenes = overlayScenes,
        currentScene = currentScene,
        previousScenes = previousScenes,
    )
}

/**
 * Applies [sceneDecoratorStrategies] to [scene] in list order: the first strategy produces the
 * innermost wrapper. An overlay scene is returned unchanged.
 *
 * `decorateScene` runs before [NavigationDisplay] provides [LocalNavigationContainer] and
 * [LocalNavigationContext] for scene content, so each `decorateScene` call is wrapped in a
 * provider for both locals.
 */
@Composable
private fun applyDecorators(
    scope: SceneDecoratorStrategyScope,
    scene: NavigationScene,
    sceneDecoratorStrategies: List<SceneDecoratorStrategy>,
    containerState: NavigationContainerState,
): NavigationScene {
    if (scene is NavigationScene.Overlay) return scene
    var result = scene
    for (decorator in sceneDecoratorStrategies) {
        var decorated: NavigationScene = result
        CompositionLocalProvider(
            LocalNavigationContainer provides containerState,
            LocalNavigationContext provides containerState.context,
        ) {
            decorated = with(decorator) { scope.decorateScene(result) }
        }
        result = decorated
    }
    return result
}

/**
 * Returns the scenes for [destinations], top-most first. Each scene before the last scene is an
 * overlay. The last scene is an overlay only when that overlay's `overlaidEntries` is empty.
 */
@Composable
private fun resolveSceneChain(
    scope: SceneStrategyScope,
    destinations: List<NavigationDestination<NavigationKey>>,
    sceneStrategy: NavigationSceneStrategy,
): List<NavigationScene> {
    val allScenes = mutableListOf<NavigationScene>()
    allScenes += sceneStrategy.calculateSceneWithSinglePaneFallback(scope, destinations)
    while (true) {
        val last = allScenes.last()
        if (last !is NavigationScene.Overlay || last.overlaidEntries.isEmpty()) break
        allScenes += sceneStrategy.calculateSceneWithSinglePaneFallback(scope, last.overlaidEntries)
    }
    return allScenes
}

@Composable
private fun computePreviousScenes(
    scope: SceneStrategyScope,
    decoratorScope: SceneDecoratorStrategyScope,
    scene: NavigationScene,
    sceneStrategy: NavigationSceneStrategy,
    sceneDecoratorStrategies: List<SceneDecoratorStrategy>,
    containerState: NavigationContainerState,
): List<NavigationScene> {
    val result = mutableListOf<NavigationScene>()
    var entries = scene.previousEntries
    while (entries.isNotEmpty()) {
        val previous = sceneStrategy.calculateSceneWithSinglePaneFallback(scope, entries)
        val decorated = applyDecorators(decoratorScope, previous, sceneDecoratorStrategies, containerState)
        result += decorated
        entries = decorated.previousEntries
    }
    return result
}
