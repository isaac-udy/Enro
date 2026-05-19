package dev.enro.ui.scenes

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.window.Dialog
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy

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
 * Enter + exit transition pair carried in a [NavigationDestination]'s
 * metadata for [DirectOverlayScene]s that opt into animated
 * enter/exit. The overlay renderer reads this off the scene's top
 * entry and forwards both transitions to the underlying
 * `AnimatedVisibility`.
 *
 * Stored as a plain (non-serialisable) value — same trade-off as
 * Dialog's `DialogProperties` metadata. Lives for the lifetime of
 * the destination only.
 */
@Immutable
public data class OverlayTransitions(
    val enter: EnterTransition,
    val exit: ExitTransition,
)

/**
 * Metadata key under which [OverlayTransitions] are stored when a
 * destination opts in via [directOverlay] / [directOverlayWithFade].
 * Public so the navigation display can read the value; not intended
 * for direct use by feature code.
 */
public const val OverlayTransitionsKey: String = "dev.enro.ui.scenes.OverlayTransitionsKey"

/**
 * A [NavigationSceneStrategy] that displays entries that have added [dialog] to their metadata
 * within a [Dialog] instance.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
public class DirectOverlaySceneStrategy : NavigationSceneStrategy {
    @Composable
    public override fun calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        val lastEntry = entries.lastOrNull()

        val directOverlayMetadata = lastEntry?.metadata?.get(DirectOverlayKey) as? Unit
        val isDirectOverlay = directOverlayMetadata != null

        return if (isDirectOverlay) {
            DirectOverlayScene(
                key = lastEntry.instance.id,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
            )
        } else null
    }

    public companion object {
        internal const val DirectOverlayKey = "dev.enro.ui.scenes.DirectOverlayKey"

        /**
         * Function to create a metadata map with dialog properties to mark this entry as something that
         * should be displayed within a [Dialog].
         *
         * @param dialogProperties properties that should be passed to the containing [Dialog].
         */
        public fun overlay(): Pair<String, Unit> = DirectOverlayKey to Unit
    }
}

/**
 * Marks the destination as a direct overlay — rendered on top of the
 * underlying scene with no shell or window wrapper. Snaps in and out
 * by default. Pair with [directOverlayWithFade] (or the explicit
 * `enter` / `exit` overload below) to animate the appearance.
 */
public fun NavigationDestination.MetadataBuilder<*>.directOverlay() {
    add(DirectOverlaySceneStrategy.overlay())
}

/**
 * Marks the destination as a direct overlay AND attaches a pair of
 * transitions for the renderer to apply when the scene appears and
 * disappears. The exit transition runs even when the destination is
 * removed externally (back press, sibling navigation, programmatic
 * close) — the renderer keeps the scene composed for the duration of
 * the transition.
 */
public fun NavigationDestination.MetadataBuilder<*>.directOverlay(
    enter: EnterTransition,
    exit: ExitTransition,
) {
    add(DirectOverlaySceneStrategy.overlay())
    add(OverlayTransitionsKey to OverlayTransitions(enter, exit))
}

/**
 * Shortcut for [directOverlay] with a symmetric fade-in / fade-out
 * pair — the most common overlay treatment. Override [durationMillis]
 * to tighten or stretch the fade; pass explicit transitions to the
 * other overload when you need anything fancier (slide, scale, etc).
 */
public fun NavigationDestination.MetadataBuilder<*>.directOverlayWithFade(
    durationMillis: Int = 128,
) {
    directOverlay(
        enter = fadeIn(animationSpec = tween(durationMillis)),
        exit = fadeOut(animationSpec = tween(durationMillis)),
    )
}

public fun NavigationDestination<*>.isDirectOverlay(): Boolean {
    return metadata[DirectOverlaySceneStrategy.DirectOverlayKey] != null
}
