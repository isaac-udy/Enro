package dev.enro.core.internal

// Original JS reference
private external class WeakRef {
    constructor(target: JsAny)
    fun deref(): JsAny
}

internal actual class EnroWeakReference<T : Any> actual constructor(referent: T?) {

    private val reference = when {
        referent != null -> WeakRef(referent.toJsReference())
        else -> null
    }

    actual fun clear() {
        TODO()
    }

    actual fun get(): T? {
        return reference?.deref()?.unsafeCast<JsReference<T>>()?.get()
    }
}