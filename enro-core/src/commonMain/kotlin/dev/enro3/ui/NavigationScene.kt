package dev.enro3.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro3.NavigationKey


public interface NavigationScene {
    public val key: Any
    public val entries: List<NavigationDestination<out NavigationKey>>
    public val previousEntries: List<NavigationDestination<out NavigationKey>>
    public val content: @Composable () -> Unit
}

/**
 * A specific scene to render 1 or more NavigationDestination instances as an overlay.
 *
 * It is expected that the [content] is rendered in one or more separate windows (e.g., a dialog,
 * popup window, etc.) that are visible above any additional [NavigationScene] instances calculated from the
 * [overlaidEntries].
 *
 * When processing [overlaidEntries], expect processing of each [NavigationSceneStrategy] to restart from the
 * first strategy. This may result in multiple instances of the same [OverlayNavigationScene] to be shown
 * simultaneously, making a unique [key] even more important.
 */
public interface OverlayNavigationScene : NavigationScene {

    /**
     * The NavigationDestination entries that should be handled by another [NavigationScene] that sits below this Scene.
     *
     * This *must* always be a non-empty list to correctly display entries below the overlay.
     */
    public val overlaidEntries: List<NavigationDestination<out NavigationKey>>
}

public fun interface NavigationSceneStrategy {
    @Composable
    public fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (count: Int) -> Unit
    ): NavigationScene?

    public infix fun then(sceneStrategy: NavigationSceneStrategy): NavigationSceneStrategy {
        return NavigationSceneStrategy { entries, onBack ->
            calculateScene(entries, onBack) ?: sceneStrategy.calculateScene(entries, onBack)
        }
    }
}

@Composable
internal fun NavigationSceneStrategy.calculateSceneWithSinglePaneFallback(
    entries: List<NavigationDestination<out NavigationKey>>,
    onBack: (count: Int) -> Unit,
): NavigationScene = calculateScene(entries, onBack) ?: SinglePaneScene().calculateScene(entries, onBack)

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
