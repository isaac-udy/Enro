package dev.enro.core

import kotlin.reflect.KClass

public actual fun <T : NavigationKey> NavigationKeySerializer.Companion.default(
    cls: KClass<T>,
): NavigationKeySerializer<T> {
    return forKotlinSerializer(cls)
}