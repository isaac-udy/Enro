package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope

/**
 * Must stay a data class: `NavigationDisplay` compares scenes by equality, and two scenes for
 * the same entry must be equal to render in the same `AnimatedContent` slot.
 */
internal data class SinglePaneScene(
    override val key: Any,
    val entry: NavigationDestination<NavigationKey>,
    override val previousEntries: List<NavigationDestination<NavigationKey>>,
) : NavigationScene {
    override val entries: List<NavigationDestination<NavigationKey>> = listOf(entry)
    override val content: @Composable () -> Unit = { entry.Content() }
}

/**
 * Always returns a scene that renders the last entry. Throws when `entries` is empty.
 */
public class SinglePaneSceneStrategy : NavigationSceneStrategy {
    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene {
        val last = entries.last()
        return SinglePaneScene(
            key = last.instance.id,
            entry = last,
            previousEntries = entries.dropLast(1),
        )
    }
}

