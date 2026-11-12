package dev.enro.recipes.scenedecoration.complex

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.ui.*
import dev.enro.ui.animation.NavigationAnimatedVisibility
import dev.enro.ui.scenes.isDirectOverlay

/**
 * Renders a top entry with `directOverlay()` metadata as a
 * [NavigationScene.Overlay]: a right-side drawer with a full-window scrim on
 * the Medium and Wide breakpoints, and a Material 3 [ModalBottomSheet] on
 * Mobile. A tap on the scrim or a sheet dismissal closes the entry.
 *
 * The strategy reads [dev.enro.ui.scenes.isDirectOverlay], the metadata that
 * `DirectOverlaySceneStrategy` reads. Register this strategy before or in
 * place of `DirectOverlaySceneStrategy`.
 */
internal class ShellOverlaySceneStrategy : NavigationSceneStrategy {
    @Composable
    override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        val top = entries.lastOrNull() ?: return null
        if (!top.isDirectOverlay()) return null
        return ShellOverlayScene(
            key = top.instance.id,
            entry = top,
            overlaidEntries = entries.dropLast(1),
        )
    }
}

private data class ShellOverlayScene(
    override val key: Any,
    val entry: NavigationDestination<NavigationKey>,
    override val overlaidEntries: List<NavigationDestination<NavigationKey>>,
) : NavigationScene.Overlay {
    override val entries: List<NavigationDestination<NavigationKey>> = listOf(entry)
    override val previousEntries: List<NavigationDestination<NavigationKey>> = overlaidEntries
    override val content: @Composable () -> Unit = {
        ShellOverlayContent(entry)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShellOverlayContent(entry: NavigationDestination<NavigationKey>) {
    val breakpoint = rememberShellBreakpoint()
    val container = LocalNavigationContainer.current
    val dismiss: () -> Unit = { container.execute(NavigationOperation.Close(entry.instance)) }

    when (breakpoint) {
        ShellBreakpoint.Mobile -> {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = dismiss,
                sheetState = sheetState,
            ) {
                entry.Content()
            }
        }
        else -> {
            val scrimColor = Color.Black.copy(alpha = 0.45f)
            val noRippleInteraction = remember { MutableInteractionSource() }

            Box(modifier = Modifier.fillMaxSize()) {
                NavigationAnimatedVisibility(
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(scrimColor)
                            .clickable(
                                interactionSource = noRippleInteraction,
                                indication = null,
                                onClick = dismiss,
                            ),
                    )
                }
                NavigationAnimatedVisibility(
                    modifier = Modifier.align(Alignment.TopEnd),
                    enter = fadeIn() + slideInHorizontally { it / 2 },
                    exit = fadeOut() + slideOutHorizontally { it / 2 },
                ) {
                    Surface(
                        modifier = Modifier
                            .width(380.dp)
                            .fillMaxHeight(),
                        tonalElevation = 6.dp,
                    ) {
                        entry.Content()
                    }
                }
            }
        }
    }
}
