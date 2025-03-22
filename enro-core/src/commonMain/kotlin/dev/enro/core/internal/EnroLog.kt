package dev.enro.core.internal

internal expect object EnroLog {
    fun debug(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun error(message: String, throwable: Throwable)
}