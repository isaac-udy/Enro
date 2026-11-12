package dev.enro.recipes.listdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope
import dev.enro.ui.get

internal data class ListPaneMetadata(
    val detailPlaceholder: @Composable () -> Unit,
)

// A NavigationDestination.MetadataKey gives typed access to one metadata
// value. `default` is the value for a destination that does not set the key.
internal object ListPaneKey : NavigationDestination.MetadataKey<ListPaneMetadata?>(default = null)

internal object IsDetailPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)

/**
 * Marks a destination as the list pane. [ListDetailSceneStrategy] renders
 * [detailPlaceholder] in the detail pane while the list destination is the
 * top destination.
 */
fun NavigationDestination.MetadataBuilder<*>.listPane(
    detailPlaceholder: @Composable () -> Unit = {},
) {
    add(ListPaneKey, ListPaneMetadata(detailPlaceholder = detailPlaceholder))
}

/**
 * Marks a destination as a detail pane. [ListDetailSceneStrategy] renders
 * the destination in the right pane while the destination is the top
 * destination and a [listPane] destination is below on the backstack.
 */
fun NavigationDestination.MetadataBuilder<*>.detailPane() {
    add(IsDetailPaneKey, true)
}

internal fun NavigationDestination<*>.listPaneMetadata(): ListPaneMetadata? =
    metadata[ListPaneKey]

internal fun NavigationDestination<*>.isDetailPane(): Boolean =
    metadata[IsDetailPaneKey]

/**
 * Renders a [listPane] destination on the left and a [detailPane]
 * destination or the placeholder on the right, when:
 *   - the window is at least [widthBreakpointDp] wide, and
 *   - the backstack contains a [listPane] destination, and
 *   - the top destination is that [listPane] destination or a
 *     [detailPane] destination.
 *
 * `calculateScene` returns `null` in every other case, and
 * `NavigationSceneStrategy.from` calls the next strategy in the list.
 */
class ListDetailSceneStrategy(
    private val widthBreakpointDp: Int = 600,
) : NavigationSceneStrategy {

    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        if (entries.isEmpty()) return null

        val width = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.toSize().width.toDp()
        }
        if (width < widthBreakpointDp.dp) return null

        val listIndex = entries.indexOfLast { it.listPaneMetadata() != null }
        if (listIndex < 0) return null

        val listEntry = entries[listIndex]
        val topEntry = entries.last()
        val detailEntry = topEntry.takeIf { it.isDetailPane() && it !== listEntry }
        if (listEntry != topEntry && detailEntry == null) return null

        return remember(listEntry.instance.id, detailEntry?.instance?.id) {
            object : NavigationScene {
                override val entries: List<NavigationDestination<NavigationKey>> =
                    listOfNotNull(listEntry, detailEntry)

                override val previousEntries: List<NavigationDestination<NavigationKey>> =
                    entries.dropLast(1)

                override val key: Any =
                    ListDetailSceneStrategy::class to (listEntry.instance.id to detailEntry?.instance?.id)

                override val content: @Composable (() -> Unit) = {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight(),
                        ) {
                            listEntry.Content()
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight(),
                        ) {
                            if (detailEntry != null) {
                                detailEntry.Content()
                            } else {
                                listEntry.listPaneMetadata()
                                    ?.detailPlaceholder
                                    ?.invoke()
                            }
                        }
                    }
                }
            }
        }
    }
}
