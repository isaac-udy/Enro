package dev.enro

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KClass

public object KClassSerializer : KSerializer<KClass<*>> {
    public override val descriptor: SerialDescriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
        serialName = "KClass",
        kind = kotlinx.serialization.descriptors.PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): KClass<*> {
        return Class.forName(decoder.decodeString()).kotlin
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: KClass<*>) {
        encoder.encodeString(value.java.name)
    }
}