package dev.enro.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * The Enro 2 control for how a destination is displayed.
 *
 * @deprecated In Enro 3, the metadata of a destination declares how the destination is
 * displayed. The equivalent of the [Present] direction is `directOverlay` in the destination's
 * metadata:
 * ```
 * @NavigationDestination(MyOverlayKey::class)
 * val myOverlayDestination = navigationDestination(
 *   metadata = { directOverlay() }
 * ) {
 *     // Destination content
 * }
 * ```
 */
@Deprecated(
    message = "NavigationDirection is deprecated. Destinations should define their own display behavior using metadata. Use directOverlay metadata for overlay/present behavior.",
    level = DeprecationLevel.WARNING
)
@Serializable
@Parcelize
public sealed class NavigationDirection: Parcelable {

    @Deprecated(
        message = "Push direction is no longer needed. This is the default navigation behavior.",
        level = DeprecationLevel.WARNING
    )
    @Serializable
    public data object Push : NavigationDirection()

    /**
     * @deprecated Use [dev.enro.ui.scenes.directOverlay] in the destination's metadata.
     */
    @Deprecated(
        message = "Present direction is deprecated. Use directOverlay metadata on the destination instead.",
        level = DeprecationLevel.WARNING
    )
    @Serializable
    public data object Present : NavigationDirection()

    @PublishedApi
    internal object MetadataKey : dev.enro.NavigationKey.MetadataKey<NavigationDirection?>(
        default = null,
    )
}
