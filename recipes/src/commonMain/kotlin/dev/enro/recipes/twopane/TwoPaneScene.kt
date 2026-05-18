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

// ─────────────────────────────────────────────────────────────────────
// Two-pane metadata
// ─────────────────────────────────────────────────────────────────────

private const val TwoPaneKey = "dev.enro.recipes.twopane.TwoPane"

/**
 * Marks a destination as eligible for two-pane rendering. The
 * [TwoPaneSceneStrategy] only renders two destinations side-by-side
 * when *both* of the top two backstack entries carry this metadata.
 */
fun NavigationDestination.MetadataBuilder<*>.twoPane() {
    add(TwoPaneKey to Unit)
}

internal fun NavigationDestination<*>.isTwoPane(): Boolean =
    metadata[TwoPaneKey] != null

// ─────────────────────────────────────────────────────────────────────
// The scene strategy
// ─────────────────────────────────────────────────────────────────────

/**
 * Activates when:
 *   - the window is at least [widthBreakpointDp] wide, AND
 *   - the top two backstack entries both have `twoPane()` metadata.
 *
 * When active, renders the two entries 50/50 side-by-side. Otherwise
 * returns `null` so the surrounding scene-strategy chain falls
 * through.
 */
class TwoPaneSceneStrategy(
    private val widthBreakpointDp: Int = 600,
) : NavigationSceneStrategy {

    @Composable
    override fun calculateScene(
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
                    // Only one entry is popped at a time; UX-wise, going
                    // back from a two-pane scene removes the top entry only.
                    entries.dropLast(1)

                override val key: Any =
                    TwoPaneSceneStrategy::class to (first.instance.id to second.instance.id)

                override val content: @Composable (() -> Unit) = {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            first.content()
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            second.content()
                        }
                    }
                }
            }
        }
    }
}
