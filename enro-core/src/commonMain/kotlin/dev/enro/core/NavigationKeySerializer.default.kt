package dev.enro.core

import kotlin.reflect.KClass

public inline fun <reified T : NavigationKey> NavigationKeySerializer.Companion.default(): NavigationKeySerializer<T> {
    return default(T::class)
}

public expect fun <T : NavigationKey> NavigationKeySerializer.Companion.default(
    cls: KClass<T>,
): NavigationKeySerializer<T>