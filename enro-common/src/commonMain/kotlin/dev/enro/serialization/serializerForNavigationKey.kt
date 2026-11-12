package dev.enro.serialization

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

/**
 * Returns the kotlinx.serialization serializer for [T]. On Android, when [T] has no
 * kotlinx.serialization serializer, serializerForNavigationKey returns a serializer for a
 * Parcelable NavigationKey.
 */
@AdvancedEnroApi
public expect inline fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T>

@PublishedApi
internal inline fun <reified T: NavigationKey> defaultSerializerForNavigationKey(): KSerializer<T> {
    val it = serializer(
        kClass = T::class,
        // TODO: support serializers for generic type arguments. typeOf is likely slow here;
        //  the annotation processor could generate the serializer and pass the serializer in.
        typeArgumentsSerializers = typeOf<T>().arguments.map {
            PolymorphicSerializer(Any::class)
        },
        isNullable = false
    )
    @Suppress("UNCHECKED_CAST")
    return it as KSerializer<T>
}
