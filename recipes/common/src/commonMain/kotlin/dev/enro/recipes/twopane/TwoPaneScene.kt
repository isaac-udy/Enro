package dev.enro.recipes.twopane

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

/**
 * `true` for a destination that [TwoPaneSceneStrategy] can render in a pane.
 *
 * `internal` is required for Kotlin/Native ObjC export: the compiler marks
 * the generic `NavigationDestination.MetadataKey` as `@HiddenFromObjC` and
 * rejects a public subtype of a `@HiddenFromObjC` type. Other code reads and
 * sets the key through [twoPane] and [isTwoPane].
 */
// region docs:scenes/metadata
internal object IsTwoPaneKey : NavigationDestination.MetadataKey<Boolean>(default = false)

/**
 * Marks a destination for two-pane rendering. [TwoPaneSceneStrategy]
 * renders two destinations side by side only when both of the top two
 * backstack entries have this metadata.
 */
fun NavigationDestination.MetadataBuilder<*>.twoPane() {
    add(IsTwoPaneKey, true)
}

internal fun NavigationDestination<*>.isTwoPane(): Boolean =
    metadata[IsTwoPaneKey]
// endregion

/**
 * Renders the top two backstack entries side by side at equal width when:
 *   - the window is at least [widthBreakpointDp] wide, and
 *   - the top two backstack entries both have `twoPane()` metadata.
 *
 * `calculateScene` returns `null` in every other case, and
 * `NavigationSceneStrategy.from` calls the next strategy in the list.
 */
// region docs:scenes/strategy
class TwoPaneSceneStrategy(
    private val widthBreakpointDp: Int = 600,
) : NavigationSceneStrategy {

    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        if (entries.size < 2) return null

        val width = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.toSize().width.toDp()
        }
        if (width < widthBreakpointDp.dp) return null

        val first = entries[entries.lastIndex - 1]
        val second = entries.last()
        if (!first.isTwoPane() || !second.isTwoPane()) return null

        return remember(first.instance.id, second.instance.id) {
            object : NavigationScene {
                override val entries: List<NavigationDestination<NavigationKey>> =
                    listOf(first, second)

                override val previousEntries: List<NavigationDestination<NavigationKey>> =
                    // previousEntries is the backstack after Back: Back from
                    // the two-pane scene removes the top entry only.
                    entries.dropLast(1)

                override val key: Any =
                    TwoPaneSceneStrategy::class to (first.instance.id to second.instance.id)

                override val content: @Composable (() -> Unit) = {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            first.Content()
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            second.Content()
                        }
                    }
                }
            }
        }
    }
}
// endregion
