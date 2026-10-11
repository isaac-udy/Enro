package dev.enro.ui.scenes

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Immutable
import dev.enro.ui.NavigationDestination

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
 * Optional metadata key carrying [OverlayTransitions] for animated
 * appearance/disappearance. Default `null`. Takes precedence over
 * [DirectOverlaySceneStrategy.OverlayFadeKey].
 */
public object OverlayTransitionsKey :
    NavigationDestination.MetadataKey<OverlayTransitions?>(default = null)

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
    directOverlay()
    add(OverlayTransitionsKey, OverlayTransitions(enter, exit))
}
