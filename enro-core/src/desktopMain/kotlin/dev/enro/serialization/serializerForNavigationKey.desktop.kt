package dev.enro.serialization

import dev.enro.NavigationKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal actual inline fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T> {
    val it = serializer(
        kClass = T::class,
        typeArgumentsSerializers = T::class.typeParameters.map {
            PolymorphicSerializer(Any::class)
        },
        isNullable = false
    )
    @Suppress("UNCHECKED_CAST")
    return it as KSerializer<T>
//    return serializer()
}