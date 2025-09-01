package dev.enro.core

import dev.enro.NavigationKey

public inline fun <reified T: NavigationKey> NavigationHandle.asTyped(): TypedNavigationHandle<T> {
    require(T::class.isInstance(key))
    @Suppress("UNCHECKED_CAST")
    return this as TypedNavigationHandle<T>
}