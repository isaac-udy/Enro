package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope
import dev.enro.ui.get

/**
 * A [NavigationScene.Overlay] that renders [entry] above the scenes below, with no window or
 * container around the content of [entry].
 *
 * `NavigationDisplay` animates the scene with the transitions set by `directOverlay(enter, exit)`
 * or [directOverlayWithFade], and keeps the scene composed until the exit transition finishes.
 * A scene with no transitions set appears and disappears without animation.
 *
 * The scene does not block input to the scenes below. Content that does not render a `Dialog`,
 * `ModalBottomSheet` or similar lets the user click through to the scenes below.
 */
public data class DirectOverlayScene(
    override val key: Any,
    override val previousEntries: List<NavigationDestination<NavigationKey>>,
    override val overlaidEntries: List<NavigationDestination<NavigationKey>>,
    val entry: NavigationDestination<NavigationKey>,
) : NavigationScene.Overlay {

    override val entries: List<NavigationDestination<NavigationKey>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        entry.Content()
    }
}

/**
 * Returns a [DirectOverlayScene] when the last entry is marked with [directOverlay] or
 * [directOverlayWithFade], and `null` otherwise.
 *
 * Add this strategy before every non-overlay scene strategy.
 */
public class DirectOverlaySceneStrategy : NavigationSceneStrategy {
    @Composable
    public override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        val lastEntry = entries.lastOrNull() ?: return null
        if (!lastEntry.metadata[IsDirectOverlayKey]) return null
        return DirectOverlayScene(
            key = lastEntry.instance.id,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            entry = lastEntry,
        )
    }

    /** `true` for a destination that renders as a direct overlay. Default `false`. */
    public object IsDirectOverlayKey :
        NavigationDestination.MetadataKey<Boolean>(
            default = false,
            name = "dev.enro.ui.scenes.DirectOverlaySceneStrategy.IsDirectOverlayKey",
        )

    /**
     * The fade duration in milliseconds set by [directOverlayWithFade]. Each renderer applies
     * the duration to the renderer's own fade. Default `null`.
     */
    public object OverlayFadeKey :
        NavigationDestination.MetadataKey<Int?>(
            default = null,
            name = "dev.enro.ui.scenes.DirectOverlaySceneStrategy.OverlayFadeKey",
        )

    public companion object
}

/**
 * Marks the destination as a direct overlay, see [DirectOverlayScene]. The overlay appears and
 * disappears without animation. Use [directOverlayWithFade] or `directOverlay(enter, exit)` to
 * animate the overlay.
 */
public fun NavigationDestination.MetadataBuilder<*>.directOverlay() {
    add(DirectOverlaySceneStrategy.IsDirectOverlayKey, true)
}

/**
 * Marks the destination as a direct overlay that fades in and fades out over [durationMillis].
 * For any other transition, use `directOverlay(enter, exit)` from `enro-ui`.
 */
public fun NavigationDestination.MetadataBuilder<*>.directOverlayWithFade(
    durationMillis: Int = 128,
) {
    directOverlay()
    add(DirectOverlaySceneStrategy.OverlayFadeKey, durationMillis)
}

public fun NavigationDestination<*>.isDirectOverlay(): Boolean {
    return metadata[DirectOverlaySceneStrategy.IsDirectOverlayKey]
}
