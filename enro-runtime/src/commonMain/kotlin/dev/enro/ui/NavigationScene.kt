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
     * Optional scene-level metadata. Used by [NavigationDisplay] to look up
     * per-scene transition overrides (e.g. [NavigationDisplay.TransitionKey])
     * before falling back to the container-level animations.
     *
     * Defaults to the metadata of the last entry, mirroring Nav3's
     * `Scene.metadata` default. Scene strategies that want to expose their
     * own metadata (or compose it from the entries) should override.
     */
    public val metadata: Map<String, Any>
        get() = entries.lastOrNull()?.metadata ?: emptyMap()

    /**
     * A typed key for values stored in a [NavigationScene]'s metadata map.
     * Parallel to [NavigationDestination.MetadataKey] — same shape, just
     * scoped to scene-level metadata.
     *
     * Example:
     * ```
     * object MyKey : NavigationScene.MetadataKey<String?>(default = null)
     * scene.metadata[MyKey]  // typed access
     * ```
     */
    public abstract class MetadataKey<T>(
        public val default: T,
    ) {
        public val name: String by lazy {
            this::class.qualifiedName ?: error("MetadataKeys must have a valid qualifiedName")
        }
    }

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

        /**
         * Invoked after this overlay has been popped from the backstack but
         * **before** it leaves composition. The overlay renderer awaits the
         * returned suspending value so the overlay can play its dismissal
         * animation (or any other suspending teardown work) without being
         * yanked out of composition mid-animation.
         *
         * Mirrors Nav3's `OverlayScene.onRemove`. Default is a no-op.
         */
        public suspend fun onRemove() {}
    }
}

/**
 * Typed accessor for a value stored under a [NavigationScene.MetadataKey].
 * Returns [NavigationScene.MetadataKey.default] when the key is absent.
 */
@Suppress("UNCHECKED_CAST")
public operator fun <T> Map<String, Any>.get(key: NavigationScene.MetadataKey<T>): T {
    return get(key.name) as T? ?: key.default
}

/**
 * Returns `true` if this metadata map contains a value for [key].
 */
public operator fun <T> Map<String, Any>.contains(key: NavigationScene.MetadataKey<T>): Boolean {
    return containsKey(key.name)
}

