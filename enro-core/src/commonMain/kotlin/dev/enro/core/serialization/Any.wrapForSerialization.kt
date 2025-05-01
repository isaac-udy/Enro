package dev.enro.core.serialization

internal fun Any?.wrapForSerialization(): Any {
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
        is List<*> -> WrappedList(this.map { it?.wrapForSerialization() }.toMutableList())
        is Set<*> -> WrappedSet(this.map { it?.wrapForSerialization() }.toMutableSet())
        is Map<*, *> -> WrappedMap(
            this.mapValues { it.value?.wrapForSerialization() }
                .mapKeys { it.key.wrapForSerialization() }
                .toMutableMap()
        )

        // don't wrap other types
        else -> this
    }
}