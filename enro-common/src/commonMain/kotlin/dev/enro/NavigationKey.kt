package dev.enro

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.path.PathData
import dev.enro.serialization.internalUnwrapForSerialization
import dev.enro.serialization.internalWrapForSerialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.Uuid

/**
 * The contract for a screen. The properties of a class that implements NavigationKey are
 * the arguments of the screen.
 *
 * Example:
 * ```
 * // Displays the profile of the user with the id `userId`
 * class Profile(val userId: String) : NavigationKey
 * ```
 *
 * A screen that returns a result of type `T` implements [NavigationKey.WithResult].
 *
 * Example:
 * ```
 * // The user selects a date within an optional range
 * class SelectDate(
 *    val minimumDate: LocalDate?,
 *    val maximumDate: LocalDate?,
 * ) : NavigationKey.WithResult<LocalDate>
 * ```
 */
public interface NavigationKey {

    /**
     * A [NavigationKey] for a screen that returns a result of type [T] to the caller.
     */
    public interface WithResult<T: Any> : NavigationKey

    /**
     * Converts between a path string and a [NavigationKey] of type [T]. Implement
     * PathBinding (typically as a nested `object` on the key class) and reference the
     * implementation from [dev.enro.annotations.NavigationPath.FromBinding] when the
     * property mapping of [dev.enro.annotations.NavigationPath] cannot express the path.
     *
     * [pattern] uses the Enro path pattern grammar, for example
     * `"/users/{id}?source={source?}"`.
     */
    public interface PathBinding<T : NavigationKey> {
        public val pattern: String
        public fun deserialize(data: PathData): T
        public fun serialize(builder: PathData.Builder, key: T)
    }

    /**
     * A [key] with the [metadata] that [asInstance] copies to the [NavigationKey.Instance]
     * created for the [key].
     */
    @ConsistentCopyVisibility
    public data class WithMetadata<out T : NavigationKey> internal constructor(
        val key: T,
        val metadata: Metadata,
    )

    /**
     * An entry for a [NavigationKey] in a navigation backstack. Two instances are equal
     * when the [id] of each instance is equal; equality does not compare [key] or [metadata].
     */
    @Stable
    @Immutable
    @Serializable
    public data class Instance<out T : NavigationKey> @AdvancedEnroApi constructor(
        @Polymorphic public val key: T,
        public val id: String = Uuid.random().toString(),
        public val metadata: Metadata = Metadata(),
    ) {
        @Deprecated(
            "Use 'key' instead of 'navigationKey'",
            level = DeprecationLevel.WARNING,
        )
        public val navigationKey: T get() = key

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Instance<*>

            return id == other.id
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }

    /**
     * A serializable key-value store attached to a [NavigationKey.Instance] or a
     * [NavigationKey.WithMetadata]. A [MetadataKey] reads and writes a typed value.
     *
     * [set] with a `null` value removes the entry. Values of a [TransientMetadataKey] are
     * not serialized.
     *
     * A value type that is not a standard Kotlin type requires a polymorphic serializer for
     * [Any] registered with the [NavigationController]. In debug mode, [set] verifies the
     * serializer for each value that is not stored under a [TransientMetadataKey].
     */
    @Serializable(with = Metadata.Serializer::class)
    public class Metadata internal constructor(
        @PublishedApi
        internal val map: MutableMap<String, Any>,
        internal val transientMap: MutableMap<String, Any>,
    ) {
        public constructor() : this(mutableMapOf(), mutableMapOf())

        public object Serializer : KSerializer<Metadata> {
            private val innerSerializer = MapSerializer(String.serializer(), PolymorphicSerializer(Any::class))
            override val descriptor: SerialDescriptor = innerSerializer.descriptor

            override fun serialize(encoder: Encoder, value: Metadata) {
                innerSerializer.serialize(
                    encoder = encoder,
                    value = value.map
                        .mapValues { it.value.internalWrapForSerialization() },
                )
            }

            override fun deserialize(decoder: Decoder): Metadata {
                val map = innerSerializer
                    .deserialize(decoder)
                    .mapValues { it.value.internalUnwrapForSerialization() }
                return Metadata(
                    map = map.toMutableMap(),
                    transientMap = mutableMapOf(),
                )
            }
        }

        public fun <T> get(key: MetadataKey<T>): T {
            @Suppress("UNCHECKED_CAST")
            return when (key is TransientMetadataKey<*>) {
                true -> transientMap[key.name] as T? ?: key.default
                false -> map[key.name] as T? ?: key.default
            }
        }

        public fun remove(key: MetadataKey<*>) {
            when (key is TransientMetadataKey<*>) {
                true -> transientMap.remove(key.name)
                false -> map.remove(key.name)
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        public fun <T> set(key: MetadataKey<T>, value: T) {
            NavigationKey.verifyMetadataSerialization(key, value)
            val isTransient = key is TransientMetadataKey<*>
            when (value) {
                null -> when (isTransient) {
                    true -> transientMap.remove(key.name)
                    false -> map.remove(key.name)
                }
                else -> when (isTransient) {
                    true -> transientMap.put(key.name, value)
                    false -> map.put(key.name, value)
                }
            }
        }

        public fun setFrom(other: Metadata) {
            map.clear()
            map.putAll(other.map)
            transientMap.clear()
            transientMap.putAll(other.transientMap)
        }

        public fun addFrom(other: Metadata) {
            map.putAll(other.map)
            transientMap.putAll(other.transientMap)
        }

        public fun copy(): Metadata {
            return Metadata().apply {
                setFrom(this@Metadata)
            }
        }

        override fun toString(): String {
            return (map + transientMap).toString()
        }
    }

    /**
     * A typed key for a value in [NavigationKey.Metadata]. [NavigationKey.Metadata.get]
     * returns [default] when the metadata contains no value for the key.
     *
     * Example:
     * ```
     * object IsDialog : NavigationKey.MetadataKey<Boolean>(default = false)
     * val isDialog = metadata.get(IsDialog) // false when no value is set
     * ```
     *
     * [NavigationKey.Metadata] stores and serializes the value under [name]. [name] defaults
     * to the [stableClassName] of the key class. On Kotlin/JS, [stableClassName] is the
     * simple class name, and reading [name] throws when two key classes have the same
     * [name]; pass an explicit name to one of the two key classes.
     */
    public abstract class MetadataKey<out T>(
        public val default: T,
        name: String?,
    ) {
        public constructor(default: T) : this(default, null)

        private val explicitName = name

        public val name: String by lazy {
            metadataKeyName(
                namespace = if (this is TransientMetadataKey<*>) TransientMetadataKey::class else MetadataKey::class,
                keyClass = this::class,
                explicitName = explicitName,
            )
        }
    }

    /**
     * A [MetadataKey] whose value is not serialized, so the value is absent after the
     * metadata is restored from saved state.
     */
    @AdvancedEnroApi
    public abstract class TransientMetadataKey<T>(
        default: T,
        name: String?,
    ) : MetadataKey<T>(default, name) {
        public constructor(default: T) : this(default, null)
    }

    public companion object {
        // EnroController assigns verifyMetadataSerialization.
        internal var verifyMetadataSerialization: (key: MetadataKey<Any?>, value: Any?) -> Unit = { _, _ -> }
    }
}

@AdvancedEnroApi
public fun <K : NavigationKey> K.withMetadata(): NavigationKey.WithMetadata<K> {
    return NavigationKey.WithMetadata(
        key = this,
        metadata = NavigationKey.Metadata(),
    )
}

public fun <T, K : NavigationKey> K.withMetadata(
    key: NavigationKey.MetadataKey<T>,
    value: T,
): NavigationKey.WithMetadata<K> {
    return NavigationKey.WithMetadata(
        key = this,
        metadata = NavigationKey.Metadata().apply {
            set(key, value)
        },
    )
}

public fun <T, K : NavigationKey> NavigationKey.WithMetadata<K>.withMetadata(
    key: NavigationKey.MetadataKey<T>,
    value: T,
): NavigationKey.WithMetadata<K> {
    return NavigationKey.WithMetadata(
        key = this@withMetadata.key,
        metadata = metadata.copy().apply {
            set(key, value)
        },
    )
}

public fun <K : NavigationKey> K.asInstance(): NavigationKey.Instance<K> {
    return NavigationKey.Instance(
        key = this,
    )
}

public fun <K: NavigationKey> NavigationKey.WithMetadata<K>.asInstance(): NavigationKey.Instance<K> {
    return NavigationKey.Instance(
        key = key,
        metadata = metadata,
    )
}
