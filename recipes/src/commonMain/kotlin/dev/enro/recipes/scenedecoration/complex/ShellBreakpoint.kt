package dev.enro.recipes.scenedecoration.complex

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

/**
 * The window-size buckets the shell adapts to. Both the scene strategy
 * (slot resolution) and the chrome decorator depend on the current breakpoint;
 * keeping the enum here means they agree on the same thresholds.
 */
internal enum class ShellBreakpoint {
    Mobile,
    Medium,
    Wide,
}

/**
 * Breakpoint thresholds. Defaults match Material 3 Adaptive's compact /
 * medium / expanded width classes.
 */
internal data class ShellBreakpoints(
    val mediumMinWidth: Dp = 600.dp,
    val wideMinWidth: Dp = 1200.dp,
)

/**
 * Reads the current window width and maps it to a [ShellBreakpoint] under
 * the supplied [breakpoints]. Recomposes whenever the window resizes across
 * a threshold.
 */
@Composable
internal fun rememberShellBreakpoint(
    breakpoints: ShellBreakpoints = ShellBreakpoints(),
): ShellBreakpoint {
    val width = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.toSize().width.toDp()
    }
    return when {
        width >= breakpoints.wideMinWidth -> ShellBreakpoint.Wide
        width >= breakpoints.mediumMinWidth -> ShellBreakpoint.Medium
        else -> ShellBreakpoint.Mobile
    }
}
