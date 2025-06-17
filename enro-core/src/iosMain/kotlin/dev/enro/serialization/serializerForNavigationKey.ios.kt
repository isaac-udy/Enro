package dev.enro.serialization

import dev.enro.NavigationKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

@PublishedApi
internal actual inline fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T> {
    val it = serializer(
        kClass = T::class,
        // TODO need to support generic serialization, this probably needs to be done in the
        //  annotation processor, and the serializer passed as an argument somewhere,
        //  because using typeof here is likely slow
        typeArgumentsSerializers = typeOf<T>().arguments.map {
            PolymorphicSerializer(Any::class)
        },
        isNullable = false
    )
    @Suppress("UNCHECKED_CAST")
    return it as KSerializer<T>
}