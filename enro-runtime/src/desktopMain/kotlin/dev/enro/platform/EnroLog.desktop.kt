package dev.enro.platform

import dev.enro.annotations.InternalEnroApi

@InternalEnroApi
public actual object EnroLog {
    public actual fun debug(message: String) {
        println("[Enro] debug: $message")
    }

    public actual fun warn(message: String) {
        println("[Enro]  warn: $message")
    }

    public actual fun error(message: String) {
        println("[Enro] error: $message")
    }

    public actual fun error(message: String, throwable: Throwable) {
        println("[Enro] error: $message")
    }
}