package dev.enro.core.controller.repository

import dev.enro.core.NKSerializer
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal class SerializerRepository {
    var serializersModule = SerializersModule {
        polymorphic(Any::class) {
            subclass(Boolean.serializer())
            subclass(Double.serializer())
            subclass(Float.serializer())
            subclass(Int.serializer())
            subclass(Long.serializer())
            subclass(Short.serializer())
            subclass(String.serializer())
            subclass(Byte.serializer())
            subclass(Char.serializer())
            subclass(NKSerializer)
            subclass(NavigationInstruction.Open.serializer(NavigationDirection.serializer()))
            subclass(NavigationDirection.serializer())
            subclass(NavigationDirection.Push.serializer())
            subclass(NavigationDirection.Present.serializer())
        }
    }
        private set

    fun registerSerializersModule(
        serializersModule: SerializersModule
    ) {
        this.serializersModule += serializersModule
    }
}