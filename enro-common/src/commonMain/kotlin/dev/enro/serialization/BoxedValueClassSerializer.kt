package dev.enro.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.PolymorphicModuleBuilder

/**
 * Gives a value class an object-shaped serialized form so it can participate
 * in polymorphic serialization.
 *
 * A `@JvmInline value class` normally serializes as its underlying primitive
 * (e.g. a bare JSON string). That representation has nowhere to carry a class
 * discriminator, and kotlinx's polymorphic JSON encoding silently degrades to
 * the bare literal on encode — which it then cannot decode ("Expected
 * JsonObject, but had JsonLiteral as the serialized body"). Any value class
 * stored as a polymorphic `Any` (for example in `NavigationKey.Metadata`, or
 * as a navigation result) would round-trip lossily: written fine, unreadable
 * on restore.
 *
 * Wrapping the value class's serializer in [BoxedValueClassSerializer] gives
 * it a single-field envelope:
 *
 * ```json
 * {"type": "com.example.CampaignId", "value": "abc-123"}
 * ```
 *
 * which the discriminator can attach to, restoring symmetric round-trips.
 * Registration uses the same serial name as the underlying serializer, so the
 * discriminator value is the value class's own serial name.
 *
 * This only affects polymorphic contexts — value classes used as plain typed
 * properties keep their efficient bare-literal form.
 *
 * Register via [valueClassSubclass]:
 *
 * ```kotlin
 * serializersModule(SerializersModule {
 *     polymorphic(Any::class) {
 *         valueClassSubclass(CampaignId.serializer())
 *     }
 * })
 * ```
 */
public class BoxedValueClassSerializer<T : Any>(
    private val inner: KSerializer<T>,
) : KSerializer<T> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        serialName = inner.descriptor.serialName,
    ) {
        element("value", inner.descriptor)
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, inner, value)
        }
    }

    override fun deserialize(decoder: Decoder): T {
        return decoder.decodeStructure(descriptor) {
            var result: T? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> result = decodeSerializableElement(descriptor, 0, inner)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index $index while decoding ${descriptor.serialName}")
                }
            }
            requireNotNull(result) {
                "Missing 'value' element while decoding ${descriptor.serialName}"
            }
        }
    }
}

/**
 * Registers [T] as a polymorphic subclass using a [BoxedValueClassSerializer]
 * around [serializer], so the value class survives polymorphic round-trips.
 * See [BoxedValueClassSerializer] for why plain `subclass(serializer)` is not
 * safe for value classes.
 */
public inline fun <reified T : Any> PolymorphicModuleBuilder<Any>.valueClassSubclass(
    serializer: KSerializer<T>,
) {
    subclass(T::class, BoxedValueClassSerializer(serializer))
}
