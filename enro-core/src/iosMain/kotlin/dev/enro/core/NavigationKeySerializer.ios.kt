package dev.enro.core

import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.serializers.SavedStateSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


public actual object NKSerializer : KSerializer<NavigationKey> {
    actual override val descriptor: SerialDescriptor = SavedStateSerializer.descriptor

    actual override fun deserialize(decoder: Decoder): NavigationKey {
        val state = SavedStateSerializer.deserialize(decoder)
        val keyType = state.read { getString("keyType") }
        val serialized = state.read { getSavedState("serialized") }
        return NavigationKeySerializer.serializers[keyType]?.deserialize(serialized)
            ?: error("No NavigationKeySerializer found for key type $keyType")
    }

    actual override fun serialize(encoder: Encoder, value: NavigationKey) {
        val qualifiedName = value::class.qualifiedName
            ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

        val serializer = NavigationKeySerializer.serializers[qualifiedName] as? NavigationKeySerializer<NavigationKey>
            ?: error("No NavigationKeySerializer found for key type $qualifiedName")

        SavedStateSerializer.serialize(encoder, savedState {
            putString("keyType", qualifiedName)
            putSavedState("serialized", serializer.serialize(value))
        })
    }
}