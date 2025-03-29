package dev.enro.core.internal

internal actual fun enroIdentityHashCode(obj: Any): Int {
    return System.identityHashCode(obj)
}