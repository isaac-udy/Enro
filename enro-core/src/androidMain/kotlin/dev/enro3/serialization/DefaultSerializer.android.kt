package dev.enro3.serialization

import dev.enro3.NavigationKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

public actual inline fun <reified T : NavigationKey> NavigationKey.Companion.defaultSerializer(): KSerializer<T> {
    val serializer = runCatching { serializer<T>() }
        .getOrNull()
    if (serializer != null) {
        return serializer
    }
    TODO("Provide parcelable serializer?")
}