package dev.enro.serialization

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

/**
 * This exists for the purpose of supporting Parcelable NavigationKeys on Android.
 *
 * Platforms other than Android will always delegate to defaultSerializerForNavigationKey<T>().
 */
@AdvancedEnroApi
public expect inline fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T>

@PublishedApi
internal inline fun <reified T: NavigationKey> defaultSerializerForNavigationKey(): KSerializer<T> {
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
