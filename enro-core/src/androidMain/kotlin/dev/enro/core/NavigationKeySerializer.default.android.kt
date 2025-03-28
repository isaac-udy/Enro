package dev.enro.core

import android.os.Parcelable
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

public actual fun <T : NavigationKey> NavigationKeySerializer.Companion.default(
    cls: KClass<T>,
): NavigationKeySerializer<T> {
    if (cls.isSubclassOf(Parcelable::class)) {
        return NavigationKeySerializerForParcelable(cls)
    }
    return forKotlinSerializer(cls)
}
