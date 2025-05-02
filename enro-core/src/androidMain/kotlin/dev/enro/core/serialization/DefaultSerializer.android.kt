@file:JvmName("DefaultSerializer")
package dev.enro.core.serialization

import dev.enro.core.NavigationKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

public actual inline fun <reified T : NavigationKey> NavigationKey.Companion.defaultSerializer(): KSerializer<T> {
    val serializer = runCatching { serializer<T>() }
        .getOrNull()
    if (serializer != null) {
        return serializer
    }
    return SerializerForParcelableNavigationKey(T::class)
}

/**
 * java.lang.Class function to get the serializer for a given type, for use in Java code
 * generated through KAPT.
 */
public fun <T : NavigationKey> create(type: Class<T>): KSerializer<T> {
    return serializer(
        kClass = type.kotlin,
        typeArgumentsSerializers = emptyList(),
        isNullable = false
    ) as KSerializer<T>
}
