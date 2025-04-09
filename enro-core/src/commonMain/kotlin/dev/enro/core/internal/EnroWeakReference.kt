package dev.enro.core.internal

internal expect class EnroWeakReference<T : Any>(referent: T) {
    fun clear()

    fun get(): T?
}
