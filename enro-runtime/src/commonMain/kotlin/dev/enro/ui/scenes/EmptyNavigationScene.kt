package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene

/**
 * The [NavigationScene] returned by [rememberNavigationSceneState] when the
 * container's destinations list is empty (typically because the container was
 * configured with `EmptyBehavior.allowEmpty()` and has been emptied).
 *
 * Rendering an empty container produces no output — this scene has no entries,
 * no previous entries, and a content lambda that composes nothing. It exists
 * so [NavigationDisplay] can still drive its [AnimatedContent] transition
 * through a real [NavigationScene] value rather than nullability everywhere.
 *
 * Having this short-circuit also guarantees that every `NavigationSceneStrategy`
 * implementation receives a non-empty `entries` list when its `calculateScene`
 * runs, which simplifies the strategy contract.
 */
internal data object EmptyNavigationScene : NavigationScene {
    override val key: Any = EmptyNavigationScene
    override val entries: List<NavigationDestination<NavigationKey>> = emptyList()
    override val previousEntries: List<NavigationDestination<NavigationKey>> = emptyList()
    override val content: @Composable () -> Unit = {}
}
