package dev.enro.platform

@PublishedApi
internal actual object EnroLog {
    actual fun debug(message: String) {
        println("[Enro] debug: $message")
    }

    actual fun warn(message: String) {
        println("[Enro]  warn: $message")
    }

    actual fun error(message: String) {
        println("[Enro] error: $message")
    }

    actual fun error(message: String, throwable: Throwable) {
        println("[Enro] error: $message")
    }
}