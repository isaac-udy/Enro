package dev.enro.recipes.scenedecoration.complex

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

/**
 * The window-width class of the shell. [ShellPaneSceneStrategy],
 * [ShellOverlaySceneStrategy] and [ShellSceneDecorator] read the same
 * breakpoint from [rememberShellBreakpoint].
 */
internal enum class ShellBreakpoint {
    Mobile,
    Medium,
    Wide,
}

/** The minimum window width of the Medium and Wide breakpoints. */
internal data class ShellBreakpoints(
    val mediumMinWidth: Dp = 600.dp,
    val wideMinWidth: Dp = 1200.dp,
)

/** Returns the [ShellBreakpoint] for the current window width. */
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
