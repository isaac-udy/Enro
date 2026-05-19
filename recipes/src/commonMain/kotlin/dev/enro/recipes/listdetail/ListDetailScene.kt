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
import dev.enro.ui.get

// ─────────────────────────────────────────────────────────────────────
// Pane metadata
// ─────────────────────────────────────────────────────────────────────

internal data class ListPaneMetadata(
    val detailPlaceholder: @Composable () -> Unit,
)

/**
 * Metadata: declares the destination is the "list" side of a
 * list-detail layout, and supplies the placeholder to render in the
 * detail slot when no detail destination is on the backstack.
 */
internal object ListPaneKey : NavigationDestination.MetadataKey<ListPaneMetadata?>(default = null)

/**
 * Metadata flag: declares the destination is the "detail" side of a
 * list-detail layout. Default `false`.
 */
internal object IsDetailPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)

/**
 * Marks a destination as the "list" side of a list-detail layout. The
 * [detailPlaceholder] is rendered in the detail slot when no
 * detail-pane destination is on the backstack above this one.
 */
fun NavigationDestination.MetadataBuilder<*>.listPane(
    detailPlaceholder: @Composable () -> Unit = {},
) {
    add(ListPaneKey, ListPaneMetadata(detailPlaceholder = detailPlaceholder))
}

/**
 * Marks a destination as the "detail" side of a list-detail layout. It
 * will appear in the right-hand pane when an active list-pane
 * destination is below it on the backstack.
 */
fun NavigationDestination.MetadataBuilder<*>.detailPane() {
    add(IsDetailPaneKey, true)
}

internal fun NavigationDestination<*>.listPaneMetadata(): ListPaneMetadata? =
    metadata[ListPaneKey]

internal fun NavigationDestination<*>.isDetailPane(): Boolean =
    metadata[IsDetailPaneKey]

// ─────────────────────────────────────────────────────────────────────
// The scene strategy
// ─────────────────────────────────────────────────────────────────────

/**
 * A scene strategy that renders a list pane on the left and a detail
 * pane (or its placeholder) on the right, when:
 *   - the window is at least [widthBreakpointDp] wide, AND
 *   - the backstack contains a destination tagged with [listPane].
 *
 * Returns `null` otherwise; the surrounding scene-strategy chain falls
 * through to the single-pane strategy, which renders the top entry
 * full-screen.
 */
class ListDetailSceneStrategy(
    private val widthBreakpointDp: Int = 600,
) : NavigationSceneStrategy {

    @Composable
    override fun calculateScene(
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
