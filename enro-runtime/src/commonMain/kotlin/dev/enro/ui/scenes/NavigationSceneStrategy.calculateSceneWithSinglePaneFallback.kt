package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope

@Composable
internal fun NavigationSceneStrategy.calculateSceneWithSinglePaneFallback(
    scope: SceneStrategyScope,
    entries: List<NavigationDestination<NavigationKey>>,
): NavigationScene {
    val scene = with(this) { scope.calculateScene(entries) }
    if (scene != null) return scene
    return with(SinglePaneSceneStrategy()) { scope.calculateScene(entries) }
}
