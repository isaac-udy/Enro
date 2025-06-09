package dev.enro.serialization

import dev.enro.NavigationKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

@PublishedApi
internal actual inline fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T> {
    return serializer()
}