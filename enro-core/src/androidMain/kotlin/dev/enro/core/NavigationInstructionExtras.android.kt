package dev.enro.core

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializerOrNull

@OptIn(InternalSerializationApi::class)
internal actual fun getSerializer(name: String): KSerializer<Any>? {
    val it = KSerializer::class.serializerOrNull()
    return Class.forName(name).kotlin.objectInstance as? KSerializer<Any>
}

internal actual fun serializerName(serializer: KSerializer<Any>) : String {
    return serializer::class.java.name
}