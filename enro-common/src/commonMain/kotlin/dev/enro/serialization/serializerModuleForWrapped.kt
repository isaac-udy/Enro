package dev.enro.serialization

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val serializerModuleForWrapped = SerializersModule {
    polymorphic(Any::class) {
        subclass(Unit.serializer())
        subclass(WrappedBoolean.serializer())
        subclass(WrappedDouble.serializer())
        subclass(WrappedFloat.serializer())
        subclass(WrappedInt.serializer())
        subclass(WrappedLong.serializer())
        subclass(WrappedShort.serializer())
        subclass(WrappedString.serializer())
        subclass(WrappedByte.serializer())
        subclass(WrappedChar.serializer())
        subclass(WrappedNull.serializer())

        subclass(WrappedList.serializer())
        subclass(WrappedSet.serializer())
        subclass(WrappedMap.serializer())
    }
}