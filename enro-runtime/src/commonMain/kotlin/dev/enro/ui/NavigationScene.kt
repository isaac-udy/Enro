package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.enro.NavigationKey
import dev.enro.metadataKeyName
import dev.enro.stableClassName

@Immutable
@Stable
public interface NavigationScene {
    public val key: Any
    public val entries: List<NavigationDestination<NavigationKey>>
    public val previousEntries: List<NavigationDestination<NavigationKey>>
    public val content: @Composable () -> Unit

    /**
     * [NavigationDisplay] reads transition overrides such as [NavigationDisplay.TransitionKey]
     * from the scene's metadata before using the container's animations.
     *
     * Defaults to the metadata of the last entry, or an empty map when [entries] is empty.
     */
    public val metadata: Map<String, Any>
        get() = entries.lastOrNull()?.metadata ?: emptyMap()

    /**
     * A typed key for a value in a [NavigationScene]'s metadata map. Reading the key from a
     * metadata map that has no value for the key returns [default].
     *
     * Example:
     * ```
     * object MyKey : NavigationScene.MetadataKey<String?>(default = null)
     * scene.metadata[MyKey]  // typed access
     * ```
     *
     * [name] defaults to the key's [stableClassName]; see [NavigationKey.MetadataKey] for when to
     * pass one explicitly.
     */
    public abstract class MetadataKey<T>(
        public val default: T,
        name: String?,
    ) {
        public constructor(default: T) : this(default, null)

        private val explicitName = name

        public val name: String by lazy {
            metadataKeyName(
                namespace = MetadataKey::class,
                keyClass = this::class,
                explicitName = explicitName,
            )
        }
    }

    /**
     * A scene that renders one or more destinations above the scenes calculated from
     * [overlaidEntries]. The [content] of an overlay is expected to render in a separate window
     * or layer, such as a dialog or popup.
     *
     * [overlaidEntries] are passed to the [NavigationSceneStrategy] again, starting from the
     * first strategy, so several overlays of the same type can be displayed at once. Each
     * overlay requires a unique [key].
     */
    public interface Overlay : NavigationScene {

        /**
         * The entries that the scenes below this overlay render. An empty list renders no scene
         * below this overlay.
         */
        public val overlaidEntries: List<NavigationDestination<NavigationKey>>

        /**
         * Called after this overlay is removed from the backstack. The overlay's content stays
         * composed until [onRemove] returns, so [onRemove] can suspend for a dismissal
         * animation.
         *
         * In `NavigationDisplay`, an exit transition set with `directOverlay(enter, exit)` or
         * `directOverlayWithFade` starts after [onRemove] returns. [onRemove] is cancelled if an
         * overlay with the same `key` becomes active again, or if the display leaves
         * composition.
         *
         * Navigation 3 equivalent: `OverlayScene.onRemove`.
         */
        public suspend fun onRemove() {}
    }
}

/**
 * Returns the value stored under [key], or [NavigationScene.MetadataKey.default] when the map
 * has no value for [key].
 */
@Suppress("UNCHECKED_CAST")
public operator fun <T> Map<String, Any>.get(key: NavigationScene.MetadataKey<T>): T {
    return get(key.name) as T? ?: key.default
}

/** Returns `true` if this metadata map contains a value for [key]. */
public operator fun <T> Map<String, Any>.contains(key: NavigationScene.MetadataKey<T>): Boolean {
    return containsKey(key.name)
}

