package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import dev.enro.NavigationKey
import dev.enro.ui.scenes.calculateSceneWithSinglePaneFallback

/**
 * A snapshot of the scene hierarchy for a [NavigationContainerState].
 *
 * Computed by [rememberNavigationSceneState], consumed by
 * [NavigationDisplay]. Mirrors Nav3's `SceneState<T>` — same fields,
 * same semantics, same eager `previousScenes` walk for predictive back.
 *
 * Hoist it from your composable if you want to inspect what
 * [NavigationDisplay] would render before it actually renders (e.g.
 * for tests, conditional UI, or to share scene calculation across
 * multiple displays). Otherwise [NavigationDisplay] computes its own
 * internally.
 *
 * @property entries the decorated destinations that fed into the scene
 *   calculation
 * @property overlayScenes any overlay scenes layered on top of
 *   [currentScene] (e.g. dialogs, bottom sheets)
 * @property currentScene the bottom-most non-overlay scene to render
 * @property previousScenes the chain of scenes produced by repeatedly
 *   walking `previousEntries` from [currentScene]. Used by predictive
 *   back to know what to reveal underneath the popping scene. Ordered
 *   from "one step back" first to "deepest" last.
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
 * Computes the [NavigationSceneState] for a [NavigationContainerState] by
 * resolving the overlay chain (top-down until a non-overlay scene is found)
 * and eagerly walking `previousEntries` for predictive back. Mirrors Nav3's
 * `rememberSceneState`.
 *
 * [onBack] is what the [SceneStrategyScope.onBack] handed to each strategy
 * invokes. Typically connected to the surrounding [NavigationDisplay]'s
 * navigation event system so scene-internal back affordances (drag-to-dismiss
 * sheets, custom close gestures) feed back into the regular backstack.
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
    val resolved = resolveSceneChain(scope, destinations, sceneStrategy)
    val overlayScenes = resolved.dropLast(1).filterIsInstance<NavigationScene.Overlay>()
    val currentScene = applyDecorators(decoratorScope, resolved.last(), sceneDecoratorStrategies)
    val previousScenes = computePreviousScenes(scope, decoratorScope, currentScene, sceneStrategy, sceneDecoratorStrategies)
    return NavigationSceneState(
        entries = destinations,
        overlayScenes = overlayScenes,
        currentScene = currentScene,
        previousScenes = previousScenes,
    )
}

/**
 * Folds the chain of [SceneDecoratorStrategy] over [scene] in order
 * (first decorator becomes the outermost wrapper). Overlay scenes are
 * skipped — mirroring Nav3, which only applies scene decorators to
 * non-overlay scenes.
 */
@Composable
private fun applyDecorators(
    scope: SceneDecoratorStrategyScope,
    scene: NavigationScene,
    sceneDecoratorStrategies: List<SceneDecoratorStrategy>,
): NavigationScene {
    if (scene is NavigationScene.Overlay) return scene
    var result = scene
    for (decorator in sceneDecoratorStrategies) {
        result = with(decorator) { scope.decorateScene(result) }
    }
    return result
}

/**
 * Resolves the overlay chain starting from `destinations`, returning a list
 * ordered [top-most ... bottom-most]. The last element is always a
 * non-overlay scene.
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

/**
 * Walks `previousEntries` until empty, producing the list of scenes that
 * would be revealed by successive pops.
 */
@Composable
private fun computePreviousScenes(
    scope: SceneStrategyScope,
    decoratorScope: SceneDecoratorStrategyScope,
    scene: NavigationScene,
    sceneStrategy: NavigationSceneStrategy,
    sceneDecoratorStrategies: List<SceneDecoratorStrategy>,
): List<NavigationScene> {
    val result = mutableListOf<NavigationScene>()
    var entries = scene.previousEntries
    while (entries.isNotEmpty()) {
        val previous = sceneStrategy.calculateSceneWithSinglePaneFallback(scope, entries)
        val decorated = applyDecorators(decoratorScope, previous, sceneDecoratorStrategies)
        result += decorated
        entries = decorated.previousEntries
    }
    return result
}
