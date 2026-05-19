package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy

/**
 * The 1-entry [NavigationScene] returned by [SinglePaneSceneStrategy]. Mirrors
 * Nav3's `SinglePaneScene` — a top-level data class with explicit equality so
 * two scenes wrapping the same entry compare structurally and AnimatedContent
 * can collapse them into the same slot.
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
 * A [NavigationSceneStrategy] that always returns a 1-entry [SinglePaneScene]
 * displaying the topmost entry.
 */
public class SinglePaneSceneStrategy : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
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

