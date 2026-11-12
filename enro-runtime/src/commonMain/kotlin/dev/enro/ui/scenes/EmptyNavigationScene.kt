package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene

/**
 * The scene that `rememberNavigationSceneState` returns for a container with no destinations,
 * for example a container using `EmptyBehavior.allowEmpty()`. `NavigationDisplay` animates to
 * and from this scene like any other scene.
 */
internal data object EmptyNavigationScene : NavigationScene {
    override val key: Any = EmptyNavigationScene
    override val entries: List<NavigationDestination<NavigationKey>> = emptyList()
    override val previousEntries: List<NavigationDestination<NavigationKey>> = emptyList()
    override val content: @Composable () -> Unit = {}
}
