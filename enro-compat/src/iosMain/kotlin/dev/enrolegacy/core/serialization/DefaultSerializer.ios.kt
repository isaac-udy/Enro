package dev.enrolegacy.core.serialization

import dev.enro.core.NavigationKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

public actual inline fun <reified T : NavigationKey> NavigationKey.Companion.defaultSerializer(): KSerializer<T> {
    return serializer()
}