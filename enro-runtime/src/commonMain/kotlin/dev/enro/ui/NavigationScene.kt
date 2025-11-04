package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.enro.NavigationKey

@Immutable
@Stable
public interface NavigationScene {
    public val key: Any
    public val entries: List<NavigationDestination<NavigationKey>>
    public val previousEntries: List<NavigationDestination<NavigationKey>>
    public val content: @Composable () -> Unit

    /**
     * A specific scene to render 1 or more NavigationDestination instances as an overlay.
     *
     * It is expected that the [content] is rendered in one or more separate windows (e.g., a dialog,
     * popup window, etc.) that are visible above any additional [NavigationScene] instances calculated from the
     * [overlaidEntries].
     *
     * When processing [overlaidEntries], expect processing of each [NavigationSceneStrategy] to restart from the
     * first strategy. This may result in multiple instances of the same [OverlayNavigationScene] to be shown
     * simultaneously, making a unique [key] even more important.
     */
    public interface Overlay : NavigationScene {

        /**
         * The NavigationDestination entries that should be handled by another [NavigationScene] that sits below this Scene.
         *
         * This *must* always be a non-empty list to correctly display entries below the overlay.
         */
        public val overlaidEntries: List<NavigationDestination<NavigationKey>>
    }
}

