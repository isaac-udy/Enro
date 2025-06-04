package dev.enro.serialization

internal fun Any?.unwrapForSerialization(): Any {
    return when (this) {
        // primitives
        null -> WrappedNull
        is WrappedBoolean -> value
        is WrappedDouble -> value
        is WrappedFloat -> value
        is WrappedInt -> value
        is WrappedLong -> value
        is WrappedShort -> value
        is WrappedString -> value
        is WrappedByte -> value
        is WrappedChar -> value

        // collections
        is WrappedList -> value.map { it?.unwrapForSerialization() }.toMutableList()
        is WrappedSet -> value.map { it?.unwrapForSerialization() }.toMutableSet()
        is WrappedMap -> value.mapValues { it.value?.unwrapForSerialization() }
            .mapKeys { it.key.unwrapForSerialization() }
            .toMutableMap()

        else -> this
    }
}