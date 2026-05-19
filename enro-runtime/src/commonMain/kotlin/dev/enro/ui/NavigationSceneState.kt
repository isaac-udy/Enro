package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
 */
@Composable
public fun rememberNavigationSceneState(
    containerState: NavigationContainerState,
    sceneStrategy: NavigationSceneStrategy,
): NavigationSceneState {
    val destinations = containerState.destinations
    val resolved = resolveSceneChain(destinations, sceneStrategy)
    val overlayScenes = resolved.dropLast(1).filterIsInstance<NavigationScene.Overlay>()
    val currentScene = resolved.last()
    val previousScenes = computePreviousScenes(currentScene, sceneStrategy)
    return NavigationSceneState(
        entries = destinations,
        overlayScenes = overlayScenes,
        currentScene = currentScene,
        previousScenes = previousScenes,
    )
}

/**
 * Resolves the overlay chain starting from `destinations`, returning a list
 * ordered [top-most ... bottom-most]. The last element is always a
 * non-overlay scene.
 */
@Composable
private fun resolveSceneChain(
    destinations: List<NavigationDestination<NavigationKey>>,
    sceneStrategy: NavigationSceneStrategy,
): List<NavigationScene> {
    val allScenes = mutableListOf<NavigationScene>()
    allScenes += sceneStrategy.calculateSceneWithSinglePaneFallback(destinations)
    while (true) {
        val last = allScenes.last()
        if (last !is NavigationScene.Overlay || last.overlaidEntries.isEmpty()) break
        allScenes += sceneStrategy.calculateSceneWithSinglePaneFallback(last.overlaidEntries)
    }
    return allScenes
}

/**
 * Walks `previousEntries` until empty, producing the list of scenes that
 * would be revealed by successive pops.
 */
@Composable
private fun computePreviousScenes(
    scene: NavigationScene,
    sceneStrategy: NavigationSceneStrategy,
): List<NavigationScene> {
    val result = mutableListOf<NavigationScene>()
    var entries = scene.previousEntries
    while (entries.isNotEmpty()) {
        val previous = sceneStrategy.calculateSceneWithSinglePaneFallback(entries)
        result += previous
        entries = previous.previousEntries
    }
    return result
}
