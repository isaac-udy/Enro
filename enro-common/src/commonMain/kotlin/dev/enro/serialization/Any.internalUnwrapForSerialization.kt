package dev.enro.serialization

internal fun Any?.internalUnwrapForSerialization(): Any {
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
        is WrappedList -> value.map { it?.internalUnwrapForSerialization() }.toMutableList()
        is WrappedSet -> value.map { it?.internalUnwrapForSerialization() }.toMutableSet()
        is WrappedMap -> value.mapValues { it.value?.internalUnwrapForSerialization() }
            .mapKeys { it.key.internalUnwrapForSerialization() }
            .toMutableMap()

        else -> this
    }
}