package dev.enro3.platform

@PublishedApi
internal actual object EnroLog {
    actual fun debug(message: String) {
        println("[Enro] DEBUG: $message")
    }

    actual fun warn(message: String) {
        println("[Enro] WARNING: $message")
    }

    actual fun error(message: String) {
        println("[Enro] ERROR: $message")
    }

    actual fun error(message: String, throwable: Throwable) {
        println("[Enro] ERROR: $message")
        throwable.printStackTrace()
    }
}