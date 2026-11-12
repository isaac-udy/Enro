@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package dev.enro.recipes.material

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationKey
import dev.enro.recipes.listdetail.isDetailPane
import dev.enro.recipes.listdetail.listPaneMetadata
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope
import dev.enro.ui.get

/**
 * A scene strategy that shows a list destination and a detail destination
 * side by side in a Material 3 `ListDetailPaneScaffold`.
 *
 * Destinations opt in with the `listPane { }` and `detailPane()` metadata
 * from the List-Detail (Scene) recipe. The strategy returns a scene when the
 * window has room for two panes; otherwise it returns `null` and the next
 * strategy (single pane) shows the top destination full screen.
 *
 * Material decides how many panes fit: `calculatePaneScaffoldDirective`
 * reads the window size class, so there is no width breakpoint in this code.
 * Enro's backstack decides what is in each pane, so the scaffold's own
 * navigator (`rememberListDetailPaneScaffoldNavigator`) is not used.
 */
class MaterialListDetailSceneStrategy : NavigationSceneStrategy {

    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        if (entries.isEmpty()) return null

        val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
        if (directive.maxHorizontalPartitions < 2) return null

        val listIndex = entries.indexOfLast { it.listPaneMetadata() != null }
        if (listIndex < 0) return null

        val listEntry = entries[listIndex]
        val topEntry = entries.last()
        val detailEntry = topEntry.takeIf { it.isDetailPane() && it !== listEntry }
        if (listEntry != topEntry && detailEntry == null) return null

        return remember(listEntry.instance.id, detailEntry?.instance?.id, directive) {
            object : NavigationScene {
                override val entries: List<NavigationDestination<NavigationKey>> =
                    listOfNotNull(listEntry, detailEntry)

                // Back from this scene removes the top entry.
                override val previousEntries: List<NavigationDestination<NavigationKey>> =
                    entries.dropLast(1)

                override val key: Any =
                    MaterialListDetailSceneStrategy::class to (listEntry.instance.id to detailEntry?.instance?.id)

                override val content: @Composable (() -> Unit) = {
                    ListDetailPaneScaffold(
                        directive = directive,
                        // Both panes are always shown: this scene only exists
                        // when there is room for two.
                        value = ThreePaneScaffoldValue(
                            primary = PaneAdaptedValue.Expanded,
                            secondary = PaneAdaptedValue.Expanded,
                            tertiary = PaneAdaptedValue.Hidden,
                        ),
                        listPane = {
                            AnimatedPane { listEntry.Content() }
                        },
                        detailPane = {
                            AnimatedPane {
                                if (detailEntry != null) {
                                    detailEntry.Content()
                                } else {
                                    listEntry.listPaneMetadata()?.detailPlaceholder?.invoke()
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

/** Metadata flag: the destination belongs in the supporting pane. */
internal object IsSupportingPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)

/**
 * Marks a destination as supporting content for the destination beneath it
 * on the backstack. With [MaterialSupportingPaneSceneStrategy] it is shown
 * beside that destination when the window has room.
 */
fun NavigationDestination.MetadataBuilder<*>.supportingPane() {
    add(IsSupportingPaneKey, true)
}

/**
 * A scene strategy that shows the top destination in the supporting pane of
 * a Material 3 `SupportingPaneScaffold`, beside the destination beneath it,
 * when the top destination has `supportingPane()` metadata and the window
 * has room for two panes.
 */
class MaterialSupportingPaneSceneStrategy : NavigationSceneStrategy {

    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        if (entries.size < 2) return null

        val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
        if (directive.maxHorizontalPartitions < 2) return null

        val supportingEntry = entries.last()
        if (!supportingEntry.metadata[IsSupportingPaneKey]) return null
        val mainEntry = entries[entries.size - 2]

        return remember(mainEntry.instance.id, supportingEntry.instance.id, directive) {
            object : NavigationScene {
                override val entries: List<NavigationDestination<NavigationKey>> =
                    listOf(mainEntry, supportingEntry)

                override val previousEntries: List<NavigationDestination<NavigationKey>> =
                    entries.dropLast(1)

                override val key: Any =
                    MaterialSupportingPaneSceneStrategy::class to (mainEntry.instance.id to supportingEntry.instance.id)

                override val content: @Composable (() -> Unit) = {
                    SupportingPaneScaffold(
                        directive = directive,
                        value = ThreePaneScaffoldValue(
                            primary = PaneAdaptedValue.Expanded,
                            secondary = PaneAdaptedValue.Expanded,
                            tertiary = PaneAdaptedValue.Hidden,
                        ),
                        mainPane = {
                            AnimatedPane { mainEntry.Content() }
                        },
                        supportingPane = {
                            AnimatedPane { supportingEntry.Content() }
                        },
                    )
                }
            }
        }
    }
}
