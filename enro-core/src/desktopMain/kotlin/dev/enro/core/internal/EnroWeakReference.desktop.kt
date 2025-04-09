package dev.enro.core.internal

internal actual class EnroWeakReference<T : Any> actual constructor(referent: T) {
    actual fun clear() {
    }

    actual fun get(): T? {
        TODO("Not yet implemented")
    }

}