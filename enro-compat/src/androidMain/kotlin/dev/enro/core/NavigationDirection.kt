package dev.enro.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * NavigationDirection was used to control how a destination is displayed when navigated to.
 *
 * @deprecated Navigation directions are no longer the recommended way to control how destinations
 * are displayed. Instead, destinations should define their own display behavior using metadata.
 *
 * For destinations that should be presented as overlays (equivalent to the old [Present] direction),
 * use the `directOverlay` metadata on the destination's definition.
 *
 * Example of the new approach:
 * ```
 * @NavigationDestination(MyOverlayKey::class)
 * val myOverlayDestination = navigationDestination(
 *   metadata = { directOverlay() }
 * ) {
 *     // Destination content
 * }
 * ```
 *
 * This approach gives destinations control over their own display behavior rather than
 * requiring the caller to specify it at navigation time.
 */
@Deprecated(
    message = "NavigationDirection is deprecated. Destinations should define their own display behavior using metadata. Use directOverlay metadata for overlay/present behavior.",
    level = DeprecationLevel.WARNING
)
@Serializable
@Parcelize
public sealed class NavigationDirection: Parcelable {

    /**
     * Push direction for standard navigation transitions.
     *
     * @deprecated Use default navigation without specifying a direction. This is the default behavior.
     */
    @Deprecated(
        message = "Push direction is no longer needed. This is the default navigation behavior.",
        level = DeprecationLevel.WARNING
    )
    @Serializable
    public data object Push : NavigationDirection()

    /**
     * Present direction for overlay/modal presentations.
     *
     * @deprecated Use [dev.enro.ui.scenes.directOverlay] metadata on the destination's NavigationKey instead.
     */
    @Deprecated(
        message = "Present direction is deprecated. Use directOverlay metadata on the destination instead.",
        level = DeprecationLevel.WARNING
    )
    @Serializable
    public data object Present : NavigationDirection()

    internal object MetadataKey : dev.enro.NavigationKey.MetadataKey<NavigationDirection?>(
        default = null,
    )
}
