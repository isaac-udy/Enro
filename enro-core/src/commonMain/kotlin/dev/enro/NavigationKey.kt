package dev.enro

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.isDebugBuild
import dev.enro.core.serialization.unwrapForSerialization
import dev.enro.core.serialization.wrapForSerialization
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.Uuid

/**
 * A NavigationKey represents the contract for a screen. A class that implements
 * the NavigationKey interface uses properties on that class to represent the
 * inputs/arguments/parameters for that contract.
 *
 * Example:
 * ```
 * // Contract for the Profile screen, which displays the profile for
 * // the user with the id passed in the "userId" parameter
 * class Profile(val userId: String) : NavigationKey
 * ```
 *
 * NavigationKeys are also able to define outputs, as well as inputs. This is done by
 * implementing the NavigationKey.WithResult<T> interface, where `T` is the type of the
 * result that is returned by that screen.
 *
 * Example:
 * ```
 * // Contract for the SelectDate screen, which allows the user to select
 * // a date within an (optional) range
 * class SelectDate(
 *    val minimumDate: LocalDate?,
 *    val maximumDate: LocalDate?,
 * ) : NavigationKey.WithResult<LocalDate>
 * ```
 *
 */
public interface NavigationKey {

    /**
     * Marks a [NavigationKey] as producing a result of type [T].
     * Implementing this interface allows the screen associated with this key
     * to return a typed value to its caller, enabling type-safe result handling.
     */
    public interface WithResult<T: Any> : NavigationKey

    /**
     * A data class that bundles a [key] of type [T] with its associated [metadata].
     * This is often used to declaratively define a navigation target along with its initial
     * metadata, before it's resolved into a [NavigationKey.Instance] by the navigation system.
     */
    @ConsistentCopyVisibility
    public data class WithMetadata<T : NavigationKey> internal constructor(
        val key: T,
        val metadata: Metadata,
    )

    /**
     * Represents a realized, active instance of a [NavigationKey] within a navigation backstack.
     * Each [NavigationKey.Instance] is uniquely identified by its [id], references the original [key]
     * it is representing, and carries its own [metadata].
     */
    @Stable
    @Immutable
    @Serializable
    @ConsistentCopyVisibility
    public data class Instance<T : NavigationKey> internal constructor(
        public val key: @Contextual T,
        public val id: String = Uuid.random().toString(),
        public val metadata: Metadata = Metadata(),
    )

    /**
     * A type-safe, serializable key-value store for attaching arbitrary data to a
     * navigation instance (either [NavigationKey.Instance] or through [NavigationKey.WithMetadata]).
     * It allows associating additional, non-contractual information with a specific
     * navigation event or screen instance, using string keys to access typed values.
     *
     * Note: When using [set], ensure that serializers for the types being stored are
     * registered with the [NavigationController] if they are not standard Kotlin types,
     * especially for polymorphic serialization of [Any]. This is checked in debug builds.
     */
    @Serializable(with = Metadata.Serializer::class)
    public class Metadata internal constructor(
        @PublishedApi
        internal val map: MutableMap<String, Any> = mutableMapOf(),
        internal val transientMap: MutableMap<String, Any> = mutableMapOf(),
    ) {
        public object Serializer : KSerializer<Metadata> {
            private val innerSerializer = MapSerializer(String.serializer(), PolymorphicSerializer(Any::class))
            override val descriptor: SerialDescriptor = innerSerializer.descriptor

            override fun serialize(encoder: Encoder, value: Metadata) {
                innerSerializer.serialize(
                    encoder = encoder,
                    value = value.map
                        .mapValues { it.value.wrapForSerialization() },
                )
            }

            override fun deserialize(decoder: Decoder): Metadata {
                val map = innerSerializer
                    .deserialize(decoder)
                    .mapValues { it.value.unwrapForSerialization() }
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
            val isTransient = key is TransientMetadataKey<*>
            if (isDebugBuild() && value != null && !isTransient) {
                val wrapped = value.wrapForSerialization()
                val hasSerializer = NavigationController.serializersModule.getPolymorphic(Any::class, wrapped) != null
                if (!hasSerializer) {
                    error("Object of type ${value::class} could not be added to NavigationKey.Metadata, make sure to register the serializer with the NavigationController.")
                }
            }
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

        public fun putAll(other: Metadata) {
            map.putAll(other.map)
            transientMap.putAll(other.transientMap)
        }

        override fun toString(): String {
            return (map + transientMap).toString()
        }
    }

    /**
     * A typed key used to access and store values within [NavigationKey.Metadata].
     *
     * Example:
     * ```
     * object IsDialog : NavigationKey.MetadataKey<Boolean>(default = false)
     * val isDialog = metadata.get(IsDialog) // isDialog will be false if not set
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
     * A TransientMetadataKey is a [MetadataKey] that is not persisted across saved instance states.
     *
     * This is marked as an [AdvancedEnroApi] because it is not recommended to use this unless you
     * understand the implications of not persisting the metadata across saved instance states.
     */
    @AdvancedEnroApi
    public abstract class TransientMetadataKey<T>(
        default: T
    ) : MetadataKey<T>(default)
}

/**
 * Creates a [NavigationKey.WithMetadata] instance from this [NavigationKey], with an empty metadata map.
 */
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
        metadata = metadata.apply {
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