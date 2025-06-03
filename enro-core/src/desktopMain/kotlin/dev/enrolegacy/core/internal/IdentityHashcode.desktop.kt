package dev.enrolegacy.core.internal

internal actual fun enroIdentityHashCode(obj: Any): Int {
    return System.identityHashCode(obj)
}