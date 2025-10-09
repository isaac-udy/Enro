package dev.enro.serialization

internal fun Any?.internalWrapForSerialization(): Any {
    return when (this) {
        // primitives
        null -> WrappedNull
        is Boolean -> WrappedBoolean(this)
        is Double -> WrappedDouble(this)
        is Float -> WrappedFloat(this)
        is Int -> WrappedInt(this)
        is Long -> WrappedLong(this)
        is Short -> WrappedShort(this)
        is String -> WrappedString(this)
        is Byte -> WrappedByte(this)
        is Char -> WrappedChar(this)

        // collections
        is List<*> -> WrappedList(this.map { it?.internalWrapForSerialization() }.toMutableList())
        is Set<*> -> WrappedSet(this.map { it?.internalWrapForSerialization() }.toMutableSet())
        is Map<*, *> -> WrappedMap(
            this.mapValues { it.value?.internalWrapForSerialization() }
                .mapKeys { it.key.internalWrapForSerialization() }
                .toMutableMap()
        )

        // don't wrap other types
        else -> this
    }
}