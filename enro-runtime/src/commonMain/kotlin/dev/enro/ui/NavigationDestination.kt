package dev.enro.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.navigationHandle

public open class NavigationDestinationProvider<T : NavigationKey>(
    private val metadata:  NavigationDestination.MetadataBuilder<T>.() -> Unit = {},
    private val content: @Composable NavigationDestinationScope<T>.() -> Unit,
) {
    public fun peekMetadata(instance: NavigationKey.Instance<T>): Map<String, Any> {
        return NavigationDestination.MetadataBuilder(instance).apply(metadata).build()
    }

    public fun create(instance: NavigationKey.Instance<T>): NavigationDestination<T> {
        return NavigationDestination.create(
            instance = instance,
            metadata = NavigationDestination.MetadataBuilder(instance).apply(metadata).build(),
            content = content,
        )
    }
}

@Stable
@Immutable
@ConsistentCopyVisibility
public data class NavigationDestination<out T : NavigationKey> private constructor(
    public val instance: NavigationKey.Instance<T>,
    public val metadata: Map<String, Any> = emptyMap(),
    private val content: @Composable () -> Unit,
) {
    public val id: String get() = instance.id
    public val key: T get() = instance.key

    /**
     * Renders this destination's content. Mirrors Nav3's `NavEntry.Content()`
     * naming so a Nav3-style scene that calls `entry.Content()` translates
     * directly to `destination.Content()` in Enro.
     */
    @Composable
    public fun Content() {
        content()
    }

    /**
     * Creates a copy of this NavigationDestination with updated metadata.
     * 
     * This function preserves the exact same instance and content references from the original
     * NavigationDestination, only replacing the metadata map. This is useful for plugins or
     * other components that need to enhance or modify the metadata associated with a destination
     * without affecting its core behavior or content.
     *
     * @param metadata The new metadata map to use in the copied NavigationDestination
     * @return A new NavigationDestination with the same instance and content, but updated metadata
     */
    internal fun copy(
        metadata: Map<String, Any>,
    ): NavigationDestination<T> {
        return NavigationDestination(
            instance = instance,
            metadata = metadata,
            content = content,
        )
    }

    public class MetadataBuilder<T : NavigationKey> internal constructor(
        public val instance: NavigationKey.Instance<T>,
    ) {
        public val key: T get() = instance.key

        private val builder: MutableMap<String, Any> = mutableMapOf()

        public fun add(key: String, value: Any) {
            builder[key] = value
        }
        public fun add(metadata: Pair<String, Any>) {
            builder[metadata.first] = metadata.second
        }

        public fun addAll(metadata: Map<String, Any>) {
            builder.putAll(metadata)
        }

        /**
         * Typed accessor for setting metadata associated with a
         * [MetadataKey]. Mirrors Nav3's `metadata { put(key, value) }`
         * DSL while keeping symmetry with [NavigationKey.MetadataKey].
         * Use [add] for string-keyed metadata.
         *
         * If [value] is `null` the entry is left absent from the
         * underlying map (`metadata[key]` will then resolve to the
         * key's default). This means `MetadataKey<X?>(default = null)`
         * works as a "key not set / value present" toggle.
         */
        public fun <V> add(key: MetadataKey<V>, value: V) {
            if (value == null) return
            @Suppress("UNCHECKED_CAST")
            builder[key.name] = value as Any
        }

        internal fun build(): Map<String, Any> = builder.toMap()
    }

    /**
     * A typed key for values stored in a [NavigationDestination]'s
     * metadata map. Mirrors Nav3's `NavMetadataKey<T>` and is named in
     * parallel with [NavigationKey.MetadataKey] (which serves the same
     * role for navigation-key metadata).
     *
     * The [default] is returned when the metadata map does not contain
     * a value for this key — i.e. for "key not set" semantics, declare
     * `MetadataKey<X?>(default = null)`.
     *
     * Example:
     * ```
     * object IsBottomSheet : NavigationDestination.MetadataKey<Boolean>(default = false)
     * val isBottomSheet = destination.metadata[IsBottomSheet]
     * ```
     */
    public abstract class MetadataKey<T>(
        public val default: T,
    ) {
        public val name: String by lazy {
            this::class.qualifiedName ?: error("MetadataKeys must have a valid qualifiedName")
        }
    }

    public companion object {
        @OptIn(ExperimentalSharedTransitionApi::class)
        internal fun <T: NavigationKey> create(
            instance: NavigationKey.Instance<T>,
            metadata: Map<String, Any> = emptyMap(),
            content: @Composable NavigationDestinationScope<T>.() -> Unit,
        ): NavigationDestination<T> {
            return NavigationDestination(
                instance = instance,
                metadata = metadata,
                content = {
                    @Suppress("UNCHECKED_CAST")
                    val navigation = navigationHandle<NavigationKey>() as NavigationHandle<T>
                    val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current
                    val sharedTransitionScope = LocalNavigationSharedTransitionScope.current
                    val scope = remember(animatedVisibilityScope, sharedTransitionScope) {
                        NavigationDestinationScope(
                            destinationMetadata = metadata,
                            navigation = navigation,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                    content.invoke(scope)
                },
            )
        }

        // createWithoutScope is used to create a NavigationDestination that does not include a NavigationDestinationScope
        // as part of the content lambda; this is important for creating decorators, as some elements required to create
        // the NavigationScope need to be provided by some of the internal decorators (e.g. lifecycle, context, etc)
        internal fun <T: NavigationKey> createWithoutScope(
            instance: NavigationKey.Instance<T>,
            metadata: Map<String, Any> = emptyMap(),
            content: @Composable () -> Unit,
        ): NavigationDestination<T> {
            return NavigationDestination(
                instance = instance,
                metadata = metadata,
                content = {
                    content.invoke()
                },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
public class NavigationDestinationScope<T : NavigationKey>(
    public val destinationMetadata: Map<String, Any>,
    public val navigation: NavigationHandle<T>,
    private val animatedVisibilityScope: AnimatedVisibilityScope,
    private val sharedTransitionScope: SharedTransitionScope,
) : SharedTransitionScope by sharedTransitionScope, AnimatedVisibilityScope by animatedVisibilityScope

// We probably want to get rid of push/present and let scenes handle those

public fun <T: NavigationKey> navigationDestination(
    metadata: NavigationDestination.MetadataBuilder<T>.() -> Unit = { },
    content: @Composable NavigationDestinationScope<T>.() -> Unit,
): NavigationDestinationProvider<T> {
    return NavigationDestinationProvider(metadata, content)
}

/**
 * Typed accessor for a [NavigationDestination.MetadataKey] stored in a
 * metadata map. Returns [NavigationDestination.MetadataKey.default] if
 * no value is present. Mirrors Nav3's `Map<String, Any>.get(NavMetadataKey<T>)`
 * extension.
 */
@Suppress("UNCHECKED_CAST")
public operator fun <T> Map<String, Any>.get(key: NavigationDestination.MetadataKey<T>): T {
    return get(key.name) as T? ?: key.default
}

/**
 * Returns `true` if this metadata map contains a value for [key].
 * Mirrors Nav3's `Map<String, Any>.contains(NavMetadataKey<T>)` extension.
 */
public operator fun <T> Map<String, Any>.contains(key: NavigationDestination.MetadataKey<T>): Boolean {
    return containsKey(key.name)
}
