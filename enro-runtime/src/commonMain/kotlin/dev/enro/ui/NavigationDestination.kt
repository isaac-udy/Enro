package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.navigationHandle
import dev.enro.metadataKeyName
import dev.enro.stableClassName

/**
 * The receiver of a destination's content, independent of how the destination is rendered.
 * [NavigationDestinationScope] adds the Compose animation scopes that [NavigationDisplay]
 * provides.
 */
public interface NavigationDestinationContentScope<T : NavigationKey> {
    public val destinationMetadata: Map<String, Any>
    public val navigation: NavigationHandle<T>
}

public open class NavigationDestinationProvider<T : NavigationKey>(
    private val metadata:  NavigationDestination.MetadataBuilder<T>.() -> Unit = {},
    private val content: @Composable NavigationDestinationContentScope<T>.() -> Unit,
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

    /** Navigation 3 equivalent: `NavEntry.Content()`. */
    @Composable
    public fun Content() {
        content()
    }

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
         * Stores [value] under [key]. A `null` [value] stores no entry and leaves an earlier
         * entry for [key] in place, so `metadata[key]` returns that entry or the key's default.
         */
        public fun <V> add(key: MetadataKey<V>, value: V) {
            if (value == null) return
            @Suppress("UNCHECKED_CAST")
            builder[key.name] = value as Any
        }

        internal fun build(): Map<String, Any> = builder.toMap()
    }

    /**
     * A typed key for a value in a [NavigationDestination]'s metadata map. Navigation 3
     * equivalent: `NavMetadataKey<T>`.
     *
     * Reading the key from a metadata map that has no value for the key returns [default].
     * Declare `MetadataKey<X?>(default = null)` to tell an unset key apart from a set value.
     *
     * Example:
     * ```
     * object IsBottomSheet : NavigationDestination.MetadataKey<Boolean>(default = false)
     * val isBottomSheet = destination.metadata[IsBottomSheet]
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

    public companion object {
        internal fun <T: NavigationKey> create(
            instance: NavigationKey.Instance<T>,
            metadata: Map<String, Any> = emptyMap(),
            content: @Composable NavigationDestinationContentScope<T>.() -> Unit,
        ): NavigationDestination<T> {
            return NavigationDestination(
                instance = instance,
                metadata = metadata,
                content = {
                    @Suppress("UNCHECKED_CAST")
                    val navigation = navigationHandle<NavigationKey>() as NavigationHandle<T>
                    val scope = remember(navigation) {
                        object : NavigationDestinationContentScope<T> {
                            override val destinationMetadata: Map<String, Any> = metadata
                            override val navigation: NavigationHandle<T> = navigation
                        }
                    }
                    content.invoke(scope)
                },
            )
        }

        // Decorators call createWithoutScope because [create] reads the navigation handle, and
        // navigationContextDecorator provides the navigation handle inside the decorator chain.
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

/**
 * Declares a destination whose content works with any renderer. Use [navigationDestination]
 * when the content needs the animation or shared-element scopes of [NavigationDisplay].
 */
public fun <T: NavigationKey> navigationDestinationContent(
    metadata: NavigationDestination.MetadataBuilder<T>.() -> Unit = { },
    content: @Composable NavigationDestinationContentScope<T>.() -> Unit,
): NavigationDestinationProvider<T> {
    return NavigationDestinationProvider(metadata, content)
}

/**
 * Returns the value stored under [key], or [NavigationDestination.MetadataKey.default] when the
 * map has no value for [key].
 */
@Suppress("UNCHECKED_CAST")
public operator fun <T> Map<String, Any>.get(key: NavigationDestination.MetadataKey<T>): T {
    return get(key.name) as T? ?: key.default
}

/** Returns `true` if this metadata map contains a value for [key]. */
public operator fun <T> Map<String, Any>.contains(key: NavigationDestination.MetadataKey<T>): Boolean {
    return containsKey(key.name)
}
