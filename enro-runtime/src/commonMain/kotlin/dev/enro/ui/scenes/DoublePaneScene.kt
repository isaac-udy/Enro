package dev.enro.ui.scenes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy

/**
 * The 2-entry [NavigationScene] returned by [DoublePaneScene]. Decides
 * between a side-by-side and single-pane layout based on the available
 * window width.
 */
internal data class DoublePaneSceneState(
    override val key: Any,
    val firstEntry: NavigationDestination<NavigationKey>?,
    val secondEntry: NavigationDestination<NavigationKey>,
    override val previousEntries: List<NavigationDestination<NavigationKey>>,
) : NavigationScene {

    override val entries: List<NavigationDestination<NavigationKey>> =
        listOfNotNull(firstEntry, secondEntry)

    override val content: @Composable () -> Unit = {
        val width = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.toSize().width.toDp()
        }
        Row {
            if (width > 600.dp && firstEntry != null) {
                Box(modifier = Modifier.weight(1f)) { firstEntry.Content() }
                Box(modifier = Modifier.weight(1f)) { secondEntry.Content() }
            } else {
                Box(modifier = Modifier.weight(1f)) { secondEntry.Content() }
            }
        }
    }
}

public class DoublePaneScene : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene {
        val secondEntry = entries.last()
        val firstEntry = entries.dropLast(1).lastOrNull()
        return DoublePaneSceneState(
            key = DoublePaneScene::class to entries.map { it.instance.id },
            firstEntry = firstEntry,
            secondEntry = secondEntry,
            previousEntries = entries.dropLast(1),
        )
    }
}