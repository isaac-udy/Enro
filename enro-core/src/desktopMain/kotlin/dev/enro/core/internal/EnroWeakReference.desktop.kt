package dev.enro.core.internal

import java.lang.ref.WeakReference

internal actual class EnroWeakReference<T : Any> actual constructor(referent: T?) {
    private val weakReference = WeakReference(referent)
    actual fun clear() {
        weakReference.clear()
    }

    actual fun get(): T? {
        return weakReference.get()
    }
}