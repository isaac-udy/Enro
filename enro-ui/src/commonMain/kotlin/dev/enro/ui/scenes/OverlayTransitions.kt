package dev.enro.ui.scenes

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Immutable
import dev.enro.ui.NavigationDestination
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * The enter and exit transitions of a [DirectOverlayScene], stored in the metadata of a
 * [NavigationDestination] under [OverlayTransitionsKey]. `NavigationDisplay` reads the
 * transitions from the last entry of the scene.
 *
 * OverlayTransitions is not serializable and is not restored with the destination.
 */
@Immutable
public data class OverlayTransitions(
    val enter: EnterTransition,
    val exit: ExitTransition,
)

/**
 * Metadata key for the [OverlayTransitions] of a direct overlay. The default is `null`.
 * A non-null value takes precedence over [DirectOverlaySceneStrategy.OverlayFadeKey].
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
public object OverlayTransitionsKey :
    NavigationDestination.MetadataKey<OverlayTransitions?>(
        default = null,
        name = "dev.enro.ui.scenes.OverlayTransitionsKey",
    )

/**
 * Marks the destination as a direct overlay, with [enter] and [exit] applied when the
 * overlay scene appears and disappears. `NavigationDisplay` keeps the scene composed until
 * [exit] finishes, whichever operation removed the destination. [exit] starts after the
 * scene's [dev.enro.ui.NavigationScene.Overlay.onRemove] returns.
 */
public fun NavigationDestination.MetadataBuilder<*>.directOverlay(
    enter: EnterTransition,
    exit: ExitTransition,
) {
    directOverlay()
    add(OverlayTransitionsKey, OverlayTransitions(enter, exit))
}
