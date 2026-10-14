package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope
import dev.enro.ui.get

/**
 * A [NavigationScene.Overlay] that renders the overlaid content directly on top of the current scene, and
 * leaves it up to the [NavigationDestination] to decide how exactly to render the content.
 *
 * When the destination's metadata supplies [OverlayTransitions] (see
 * the [directOverlay] / [directOverlayWithFade] builders), Enro's
 * overlay renderer applies the given enter / exit transitions to the
 * scene's appearance and disappearance — including the disappearance
 * path, by keeping the scene composed for the duration of the exit
 * transition. Without those transitions the scene snaps in and out
 * the way it always did.
 *
 * If the content in the [DirectOverlayScene] does not prevent the user from interacting with the underlying
 * scene (e.g. by using a Dialog or ModalBottomSheet), it will be possible to click through the overlay
 * and interact with the underlying scene.
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
 * A [NavigationSceneStrategy] that displays entries which have opted
 * into direct-overlay rendering via [directOverlay] / [directOverlayWithFade].
 *
 * This strategy should always be added before any non-overlay scene strategies.
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

    /**
     * Metadata flag indicating that a destination should be rendered
     * as a direct overlay. Default `false`.
     */
    public object IsDirectOverlayKey :
        NavigationDestination.MetadataKey<Boolean>(
            default = false,
            name = "dev.enro.ui.scenes.DirectOverlaySceneStrategy.IsDirectOverlayKey",
        )

    /**
     * Optional fade duration, in milliseconds, set by [directOverlayWithFade]. Each renderer
     * maps it to its own fade. Default `null`.
     */
    public object OverlayFadeKey :
        NavigationDestination.MetadataKey<Int?>(
            default = null,
            name = "dev.enro.ui.scenes.DirectOverlaySceneStrategy.OverlayFadeKey",
        )

    public companion object
}

/**
 * Marks the destination as a direct overlay — rendered on top of the
 * underlying scene with no shell or window wrapper. Snaps in and out
 * by default. Pair with [directOverlayWithFade] (or the explicit
 * `enter` / `exit` overload) to animate the appearance.
 */
public fun NavigationDestination.MetadataBuilder<*>.directOverlay() {
    add(DirectOverlaySceneStrategy.IsDirectOverlayKey, true)
}

/**
 * Shortcut for [directOverlay] with a symmetric fade-in / fade-out
 * pair — the most common overlay treatment. Override [durationMillis]
 * to tighten or stretch the fade; Compose UI destinations can pass
 * explicit transitions to `directOverlay(enter, exit)` when they need
 * anything fancier (slide, scale, etc).
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
