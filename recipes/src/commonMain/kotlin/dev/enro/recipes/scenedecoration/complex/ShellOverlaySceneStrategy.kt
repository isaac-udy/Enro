package dev.enro.recipes.scenedecoration.complex

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope
import dev.enro.ui.scenes.isDirectOverlay

/**
 * Renders a `directOverlay()`-tagged top entry as a shell overlay:
 * right-side drawer (with full-window scrim) on Medium / Wide breakpoints,
 * bottom sheet on Mobile. Tap-on-scrim closes the overlay.
 *
 * Reuses [dev.enro.ui.scenes.isDirectOverlay] so the recipe doesn't introduce
 * a new metadata key — destinations tagged with the existing
 * `directOverlay()` builder from `dev.enro.ui.scenes` work as-is. Register
 * this strategy before (or instead of) `DirectOverlaySceneStrategy` in the
 * chain.
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

@Composable
private fun ShellOverlayContent(entry: NavigationDestination<NavigationKey>) {
    val breakpoint = rememberShellBreakpoint()
    val container = LocalNavigationContainer.current
    val scrimColor = Color.Black.copy(alpha = 0.45f)
    val dismiss: () -> Unit = { container.execute(NavigationOperation.Close(entry.instance)) }
    val noRippleInteraction = remember { MutableInteractionSource() }

    when (breakpoint) {
        ShellBreakpoint.Mobile -> {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(scrimColor)
                        .clickable(
                            interactionSource = noRippleInteraction,
                            indication = null,
                            onClick = dismiss,
                        ),
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    tonalElevation = 6.dp,
                ) {
                    entry.Content()
                }
            }
        }
        else -> {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(scrimColor)
                        .clickable(
                            interactionSource = noRippleInteraction,
                            indication = null,
                            onClick = dismiss,
                        ),
                )
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
