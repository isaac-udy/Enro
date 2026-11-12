package dev.enro.recipes.scenedecoration.complex

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope

/**
 * Assigns the top backstack entries to the slots `(left?, main, right?)` and
 * renders the slots in a [Row]. The package `README.md` documents the slot
 * resolution in full. In short:
 *
 *   1. A top entry with `rightPane()` fills the right slot and the entry
 *      below fills main. Otherwise the top entry fills main.
 *   2. The entry directly below main fills the left slot when that entry has
 *      `leftPane()`.
 *   3. The breakpoint limits the slots. Mobile: the top entry only. Medium:
 *      main and one side pane, with right selected before left. Wide: all
 *      three slots.
 *   4. A `fullScreen()` top entry fills main with no side panes.
 */
internal class ShellPaneSceneStrategy(
    private val breakpoints: ShellBreakpoints = ShellBreakpoints(),
) : NavigationSceneStrategy {

    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        if (entries.isEmpty()) return null
        val breakpoint = rememberShellBreakpoint(breakpoints)
        val slots = resolveSlots(entries, breakpoint)
        return ShellPaneScene(
            slots = slots,
            previousEntries = entries.dropLast(1),
        )
    }
}

internal data class ShellPaneSlots(
    val left: NavigationDestination<NavigationKey>?,
    val main: NavigationDestination<NavigationKey>,
    val right: NavigationDestination<NavigationKey>?,
) {
    val asList: List<NavigationDestination<NavigationKey>> = listOfNotNull(left, main, right)
}

internal fun resolveSlots(
    entries: List<NavigationDestination<NavigationKey>>,
    breakpoint: ShellBreakpoint,
): ShellPaneSlots {
    val top = entries.last()
    if (top.isFullScreen) {
        return ShellPaneSlots(left = null, main = top, right = null)
    }
    val (main, right) = if (top.isRightPane && entries.size >= 2) {
        entries[entries.size - 2] to top
    } else {
        top to null
    }
    val mainIndex = entries.indexOf(main)
    val left = if (mainIndex > 0) {
        entries[mainIndex - 1].takeIf { it.isLeftPane }
    } else {
        null
    }
    return when (breakpoint) {
        ShellBreakpoint.Mobile -> ShellPaneSlots(null, top, null)
        ShellBreakpoint.Medium -> when {
            right != null -> ShellPaneSlots(null, main, right)
            left != null -> ShellPaneSlots(left, main, null)
            else -> ShellPaneSlots(null, main, null)
        }
        ShellBreakpoint.Wide -> ShellPaneSlots(left, main, right)
    }
}

private data class ShellPaneScene(
    val slots: ShellPaneSlots,
    override val previousEntries: List<NavigationDestination<NavigationKey>>,
) : NavigationScene {
    override val entries: List<NavigationDestination<NavigationKey>> = slots.asList

    override val key: Any = ShellPaneScene::class to Triple(
        slots.left?.instance?.id,
        slots.main.instance.id,
        slots.right?.instance?.id,
    )

    override val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxSize()) {
            val left = slots.left
            val right = slots.right
            if (left != null) {
                Box(modifier = Modifier.weight(0.32f).fillMaxHeight()) { left.Content() }
                VerticalDivider()
            }
            val mainWeight = when {
                left != null && right != null -> 0.36f
                left != null || right != null -> 0.68f
                else -> 1f
            }
            Box(modifier = Modifier.weight(mainWeight).fillMaxHeight()) { slots.main.Content() }
            if (right != null) {
                VerticalDivider()
                Box(modifier = Modifier.weight(0.32f).fillMaxHeight()) { right.Content() }
            }
        }
    }
}
