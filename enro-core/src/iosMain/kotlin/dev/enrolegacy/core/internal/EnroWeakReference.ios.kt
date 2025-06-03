package dev.enrolegacy.core.internal

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

@OptIn(ExperimentalNativeApi::class)
internal actual class EnroWeakReference<T : Any> actual constructor(referent: T?) {
    private val internal = referent?.let { WeakReference(it) }

    actual fun clear() {
        internal?.clear()
    }

    actual fun get(): T? {
        return internal?.get()
    }

}