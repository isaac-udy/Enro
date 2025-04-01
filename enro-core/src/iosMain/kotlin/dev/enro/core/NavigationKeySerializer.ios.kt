package dev.enro.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public actual object NKSerializer : KSerializer<NavigationKey> {
    actual override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    actual override fun deserialize(decoder: Decoder): NavigationKey {
        TODO("Not yet implemented")
    }

    actual override fun serialize(encoder: Encoder, value: NavigationKey) {
    }
}