package dev.enro.platform

import dev.enro.annotations.InternalEnroApi

@InternalEnroApi
public actual object EnroLog {
    public actual fun debug(message: String) {
        println("[Enro] DEBUG: $message")
    }

    public actual fun warn(message: String) {
        println("[Enro] WARNING: $message")
    }

    public actual fun error(message: String) {
        println("[Enro] ERROR: $message")
    }

    public actual fun error(message: String, throwable: Throwable) {
        println("[Enro] ERROR: $message")
        throwable.printStackTrace()
    }
}